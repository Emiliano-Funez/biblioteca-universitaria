package com.itu.biblioteca.service;

import com.itu.biblioteca.dao.MaterialDAO;
import com.itu.biblioteca.dao.PrestamoDAO;
import com.itu.biblioteca.dao.UsuarioDAO;
import com.itu.biblioteca.exception.LimitePrestamosException;
import com.itu.biblioteca.exception.MaterialNoCompatibleException;
import com.itu.biblioteca.exception.MaterialNoDisponibleException;
import com.itu.biblioteca.exception.UsuarioSuspendidoException;
import com.itu.biblioteca.model.MaterialBibliografico;
import com.itu.biblioteca.model.Multa;
import com.itu.biblioteca.model.Prestamo;
import com.itu.biblioteca.model.Usuario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PrestamoService {

    private final UsuarioDAO usuarioDAO;
    private final MaterialDAO materialDAO;
    private final PrestamoDAO prestamoDAO;
    private final MultaService multaService;

    public PrestamoService() {
        this.usuarioDAO = new UsuarioDAO();
        this.materialDAO = new MaterialDAO();
        this.prestamoDAO = new PrestamoDAO();
        this.multaService = new MultaService();
    }

    public Prestamo registrarPrestamo(
            int usuarioId,
            String codigoMaterial)
            throws SQLException,
            UsuarioSuspendidoException,
            LimitePrestamosException,
            MaterialNoDisponibleException,
            MaterialNoCompatibleException {

        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario no existe."
            );
        }

        if (!usuario.isActivo()) {
            throw new UsuarioSuspendidoException(
                    "El usuario se encuentra suspendido."
            );
        }

        MaterialBibliografico material =
                materialDAO.buscarPorCodigo(codigoMaterial);

        if (material == null) {
            throw new IllegalArgumentException(
                    "El material no existe."
            );
        }

        int prestamosActivos =
                prestamoDAO.contarPrestamosActivosPorUsuario(
                        usuarioId
                );

        if (prestamosActivos >= usuario.getLimitePrestamos()) {
            throw new LimitePrestamosException(
                    "El usuario alcanzó su límite de préstamos."
            );
        }

        if (material.getCantidadDisponible() <= 0) {
            throw new MaterialNoDisponibleException(
                    "El material no tiene unidades disponibles."
            );
        }

        if (!material.esCompatibleCon(usuario)) {
            throw new MaterialNoCompatibleException(
                    "El tipo de usuario "
                            + usuario.getTipoUsuario()
                            + " no puede solicitar un "
                            + material.getTipoMaterial()
                            + "."
            );
        }

        Prestamo prestamo = new Prestamo(
                0,
                usuario,
                material,
                LocalDate.now()
        );

        material.disminuirDisponibilidad();

        /*
         * Este DAO realiza INSERT del préstamo + UPDATE del stock
         * dentro de una misma transacción.
         */
        prestamoDAO.guardar(prestamo);

        return prestamo;
    }

    public Multa registrarDevolucion(int prestamoId)
            throws SQLException {

        Prestamo prestamo =
                prestamoDAO.buscarPorId(prestamoId);

        if (prestamo == null) {
            throw new IllegalArgumentException(
                    "El préstamo no existe."
            );
        }

        if (!prestamo.estaActivo()) {
            throw new IllegalArgumentException(
                    "El préstamo ya fue devuelto."
            );
        }

        LocalDate fechaDevolucion = LocalDate.now();

        prestamo.setFechaDevolucion(fechaDevolucion);
        prestamo.getMaterial().aumentarDisponibilidad();

        int diasAtraso =
                calcularDiasAtraso(
                        prestamo.getFechaVencimiento(),
                        fechaDevolucion
                );

        Multa multa = null;

        if (diasAtraso > 0) {

            double monto =
                    diasAtraso
                            * prestamo.getUsuario().getMultaPorDia();

            multa = new Multa(
                    0,
                    prestamo,
                    diasAtraso,
                    monto
            );
        }

        /*
         * Devolución + actualización de stock + multa,
         * cuando corresponde, se guardan dentro de una
         * misma transacción.
         */
        prestamoDAO.registrarDevolucion(
                prestamo,
                multa
        );

        if (multa != null) {
            multaService.evaluarSuspension(
                    prestamo.getUsuario()
            );
        }

        return multa;
    }

    public List<Prestamo> listarVencidos()
            throws SQLException {

        return prestamoDAO.listarVencidos();
    }

    public List<Prestamo> listarPrestamosPorUsuario(
            int usuarioId) throws SQLException {

        return prestamoDAO.listarPrestamosPorUsuario(
                usuarioId
        );
    }

    public List<Prestamo> listarHistorialPorMaterial(
            String codigo) throws SQLException {

        return prestamoDAO.listarHistorialPorMaterial(
                codigo
        );
    }

    public int calcularDiasAtraso(
            LocalDate vencimiento,
            LocalDate fechaEvaluacion) {

        if (!fechaEvaluacion.isAfter(vencimiento)) {
            return 0;
        }

        return (int) ChronoUnit.DAYS.between(
                vencimiento,
                fechaEvaluacion
        );
    }

    public double calcularMultaPendiente(
            Prestamo prestamo) {

        if (!prestamo.estaActivo()) {
            return 0.0;
        }

        int diasAtraso =
                calcularDiasAtraso(
                        prestamo.getFechaVencimiento(),
                        LocalDate.now()
                );

        return diasAtraso
                * prestamo.getUsuario().getMultaPorDia();
    }
}
