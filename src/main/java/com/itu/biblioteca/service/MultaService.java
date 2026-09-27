package com.itu.biblioteca.service;

import com.itu.biblioteca.dao.MultaDAO;
import com.itu.biblioteca.dao.UsuarioDAO;
import com.itu.biblioteca.model.Multa;
import com.itu.biblioteca.model.Usuario;

import java.sql.SQLException;

public class MultaService {

    public static final double UMBRAL_SUSPENSION = 5000.0;

    private final MultaDAO multaDAO;
    private final UsuarioDAO usuarioDAO;

    public MultaService() {
        this.multaDAO = new MultaDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public void registrarMulta(Multa multa)
            throws SQLException {

        if (multaDAO.existePorPrestamo(
                multa.getPrestamo().getId())) {

            return;
        }

        multaDAO.guardar(multa);
        evaluarSuspension(
                multa.getPrestamo().getUsuario()
        );
    }

    public double calcularMontoAcumulado(
            Usuario usuario) throws SQLException {

        return multaDAO.calcularMontoPendientePorUsuario(
                usuario.getId()
        );
    }

    public void evaluarSuspension(
            Usuario usuario) throws SQLException {

        double montoAcumulado =
                calcularMontoAcumulado(usuario);

        if (montoAcumulado > UMBRAL_SUSPENSION
                && usuario.isActivo()) {

            usuario.setActivo(false);
            usuarioDAO.actualizarEstado(usuario);
        }
    }
}
