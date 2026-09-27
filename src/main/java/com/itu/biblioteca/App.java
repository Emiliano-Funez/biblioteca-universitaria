package com.itu.biblioteca;

import com.itu.biblioteca.dao.MaterialDAO;
import com.itu.biblioteca.dao.MultaDAO;
import com.itu.biblioteca.dao.UsuarioDAO;
import com.itu.biblioteca.model.Docente;
import com.itu.biblioteca.model.Estudiante;
import com.itu.biblioteca.model.MaterialBibliografico;
import com.itu.biblioteca.model.Multa;
import com.itu.biblioteca.model.Prestamo;
import com.itu.biblioteca.model.Usuario;
import com.itu.biblioteca.model.Libro;
import com.itu.biblioteca.model.Revista;
import com.itu.biblioteca.service.MultaService;
import com.itu.biblioteca.service.PrestamoService;
import com.itu.biblioteca.exception.LimitePrestamosException;
import com.itu.biblioteca.exception.MaterialNoCompatibleException;
import com.itu.biblioteca.exception.MaterialNoDisponibleException;
import com.itu.biblioteca.exception.UsuarioSuspendidoException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class App {

    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final MaterialDAO materialDAO = new MaterialDAO();
    private static final MultaDAO multaDAO = new MultaDAO();

    private static final PrestamoService prestamoService =
            new PrestamoService();

    private static final MultaService multaService =
            new MultaService();

    private static final Scanner scanner =
            new Scanner(System.in);

    public static void main(String[] args) {

        int opcion;

        do {
            mostrarMenu();

            opcion = leerEntero(
                    "Seleccione una opción: "
            );

            System.out.println();

            try {
                switch (opcion) {
                    case 1 -> registrarUsuario();
                    case 2 -> modificarUsuario();
                    case 3 -> listarUsuarios();
                    case 4 -> registrarMaterial();
                    case 5 -> listarMateriales();
                    case 6 -> registrarPrestamo();
                    case 7 -> registrarDevolucion();
                    case 8 -> verPrestamosVencidos();
                    case 9 -> verHistorialPorUsuario();
                    case 10 -> verHistorialPorMaterial();
                    case 11 -> verMultasPorUsuario();
                    case 12 -> verTodasLasMultas();
                    case 13 -> pagarMulta();
                    case 14 -> System.out.println(
                            "Sistema finalizado."
                    );
                    default -> System.out.println(
                            "Opción inválida."
                    );
                }
            } catch (SQLException e) {
                System.out.println(
                        "Error de base de datos: "
                                + e.getMessage()
                );
            } catch (Exception e) {
                System.out.println(
                        "Error: "
                                + e.getMessage()
                );
            }

        } while (opcion != 14);

        scanner.close();
    }

    private static void mostrarMenu() {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "      SISTEMA DE GESTIÓN DE BIBLIOTECA"
        );
        System.out.println(
                "=============================================="
        );
        System.out.println("1.  Registrar usuario");
        System.out.println("2.  Modificar usuario");
        System.out.println("3.  Listar usuarios");
        System.out.println("4.  Registrar material");
        System.out.println("5.  Listar materiales");
        System.out.println("6.  Registrar préstamo");
        System.out.println("7.  Registrar devolución");
        System.out.println("8.  Ver préstamos vencidos");
        System.out.println("9.  Ver historial por usuario");
        System.out.println("10. Ver historial por material");
        System.out.println("11. Ver multas por usuario");
        System.out.println("12. Ver todas las multas");
        System.out.println("13. Registrar pago de multa");
        System.out.println("14. Salir");
        System.out.println(
                "=============================================="
        );
    }

    private static void registrarUsuario()
            throws SQLException {

        System.out.println(
                "--- REGISTRO DE USUARIO ---"
        );

        int id = leerEnteroPositivo(
                "ID único: "
        );

        if (usuarioDAO.buscarPorId(id) != null) {
            System.out.println(
                    "Ese ID ya está registrado."
            );
            return;
        }

        String nombre = leerTextoNoVacio(
                "Nombre: "
        );

        String apellido = leerTextoNoVacio(
                "Apellido: "
        );

        System.out.println(
                "1. Estudiante"
        );
        System.out.println(
                "2. Docente"
        );

        int tipo = leerEntero(
                "Tipo: "
        );

        Usuario usuario;

        if (tipo == 1) {
            usuario = new Estudiante(
                    id,
                    nombre,
                    apellido
            );
        } else if (tipo == 2) {
            usuario = new Docente(
                    id,
                    nombre,
                    apellido
            );
        } else {
            System.out.println(
                    "Tipo inválido."
            );
            return;
        }

        usuarioDAO.guardar(usuario);

        System.out.println(
                "Usuario registrado correctamente."
        );
    }

    private static void modificarUsuario()
            throws SQLException {

        System.out.println(
                "--- MODIFICAR USUARIO ---"
        );

        int id = leerEnteroPositivo(
                "ID del usuario: "
        );

        Usuario usuario = usuarioDAO.buscarPorId(id);

        if (usuario == null) {
            System.out.println(
                    "No existe ese usuario."
            );
            return;
        }

        System.out.println(
                "Dejá vacío un campo para conservarlo."
        );

        String nombre = leerTexto(
                "Nombre actual: " + usuario.getNombre()
                        + " | Nuevo nombre: "
        );

        String apellido = leerTexto(
                "Apellido actual: " + usuario.getApellido()
                        + " | Nuevo apellido: "
        );

        if (!nombre.isBlank()) {
            usuario.setNombre(nombre);
        }

        if (!apellido.isBlank()) {
            usuario.setApellido(apellido);
        }

        usuarioDAO.actualizarDatos(usuario);

        System.out.println();
        System.out.println("Estado actual: "
                + (usuario.isActivo()
                ? "ACTIVO"
                : "SUSPENDIDO"));

        System.out.println("1. Mantener estado");
        System.out.println("2. Activar usuario");
        System.out.println("3. Suspender usuario");

        int estado = leerEntero(
                "Seleccione: "
        );

        if (estado == 2) {

            double deuda =
                    multaService.calcularMontoAcumulado(
                            usuario
                    );

            if (deuda > MultaService.UMBRAL_SUSPENSION) {
                System.out.println(
                        "No se puede activar porque "
                                + "las multas pendientes superan "
                                + "el umbral."
                );
            } else {
                usuario.setActivo(true);
                usuarioDAO.actualizarEstado(usuario);
            }

        } else if (estado == 3) {

            usuario.setActivo(false);
            usuarioDAO.actualizarEstado(usuario);
        }

        System.out.println(
                "Usuario actualizado correctamente."
        );
    }

    private static void listarUsuarios()
            throws SQLException {

        System.out.println(
                "--- LISTADO DE USUARIOS ---"
        );

        System.out.println("1. Todos");
        System.out.println("2. Activos");
        System.out.println("3. Suspendidos");

        int opcion = leerEntero(
                "Filtro: "
        );

        List<Usuario> usuarios;

        if (opcion == 1) {
            usuarios = usuarioDAO.listarTodos();
        } else if (opcion == 2) {
            usuarios = usuarioDAO.listarPorEstado(true);
        } else if (opcion == 3) {
            usuarios = usuarioDAO.listarPorEstado(false);
        } else {
            System.out.println(
                    "Filtro inválido."
            );
            return;
        }

        if (usuarios.isEmpty()) {
            System.out.println(
                    "No hay usuarios para mostrar."
            );
            return;
        }

        for (Usuario usuario : usuarios) {
            System.out.printf(
                    "ID: %d | %s %s | Tipo: %s | "
                            + "Estado: %s | Límite: %d | "
                            + "Días: %d | Multa/día: $%.2f%n",
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getTipoUsuario(),
                    usuario.isActivo()
                            ? "ACTIVO"
                            : "SUSPENDIDO",
                    usuario.getLimitePrestamos(),
                    usuario.getDiasPrestamo(),
                    usuario.getMultaPorDia()
            );
        }
    }

    private static void registrarMaterial()
            throws SQLException {

        System.out.println(
                "--- REGISTRO DE MATERIAL ---"
        );

        String codigo = leerTextoNoVacio(
                "Código único: "
        ).toUpperCase();

        if (materialDAO.buscarPorCodigo(codigo) != null) {
            System.out.println(
                    "Ese código ya está registrado."
            );
            return;
        }

        String titulo = leerTextoNoVacio(
                "Título: "
        );

        int cantidad = leerEnteroPositivo(
                "Cantidad total: "
        );

        System.out.println("1. Libro");
        System.out.println("2. Revista");

        int tipo = leerEntero(
                "Tipo: "
        );

        MaterialBibliografico material;

        if (tipo == 1) {
            material = new Libro(
                    codigo,
                    titulo,
                    cantidad
            );
        } else if (tipo == 2) {
            material = new Revista(
                    codigo,
                    titulo,
                    cantidad
            );
        } else {
            System.out.println(
                    "Tipo inválido."
            );
            return;
        }

        materialDAO.guardar(material);

        System.out.println(
                "Material registrado correctamente."
        );
    }

    private static void listarMateriales()
            throws SQLException {

        System.out.println(
                "--- LISTADO DE MATERIALES ---"
        );

        System.out.println("1. Todos");
        System.out.println("2. Disponibles");
        System.out.println("3. No disponibles");

        int opcion = leerEntero(
                "Filtro: "
        );

        List<MaterialBibliografico> materiales;

        if (opcion == 1) {
            materiales = materialDAO.listar();
        } else if (opcion == 2) {
            materiales =
                    materialDAO.listarDisponibles();
        } else if (opcion == 3) {
            materiales =
                    materialDAO.listarNoDisponibles();
        } else {
            System.out.println(
                    "Filtro inválido."
            );
            return;
        }

        if (materiales.isEmpty()) {
            System.out.println(
                    "No hay materiales para mostrar."
            );
            return;
        }

        for (MaterialBibliografico material :
                materiales) {

            System.out.printf(
                    "Código: %s | Título: %s | Tipo: %s | "
                            + "Disponibles: %d/%d%n",
                    material.getCodigo(),
                    material.getTitulo(),
                    material.getTipoMaterial(),
                    material.getCantidadDisponible(),
                    material.getCantidadTotal()
            );
        }
    }

    private static void registrarPrestamo()
            throws SQLException,
            UsuarioSuspendidoException,
            LimitePrestamosException,
            MaterialNoDisponibleException,
            MaterialNoCompatibleException {

        System.out.println(
                "--- REGISTRO DE PRÉSTAMO ---"
        );

        int usuarioId = leerEnteroPositivo(
                "ID del usuario: "
        );

        String codigoMaterial =
                leerTextoNoVacio(
                        "Código del material: "
                ).toUpperCase();

        Prestamo prestamo =
                prestamoService.registrarPrestamo(
                        usuarioId,
                        codigoMaterial
                );

        System.out.println(
                "Préstamo registrado correctamente."
        );

        mostrarPrestamo(prestamo);
    }

    private static void registrarDevolucion()
            throws SQLException {

        System.out.println(
                "--- REGISTRO DE DEVOLUCIÓN ---"
        );

        int prestamoId = leerEnteroPositivo(
                "ID del préstamo: "
        );

        Multa multa =
                prestamoService.registrarDevolucion(
                        prestamoId
                );

        if (multa == null) {
            System.out.println(
                    "Devolución registrada sin multa."
            );
        } else {
            System.out.printf(
                    "Devolución registrada con %d días de atraso.%n",
                    multa.getDiasAtraso()
            );

            System.out.printf(
                    "Multa generada: $%.2f%n",
                    multa.getMonto()
            );

            System.out.println(
                    "La multa quedó pendiente de pago."
            );
        }
    }

    private static void verPrestamosVencidos()
            throws SQLException {

        System.out.println(
                "--- PRÉSTAMOS VENCIDOS ---"
        );

        List<Prestamo> prestamos =
                prestamoService.listarVencidos();

        if (prestamos.isEmpty()) {
            System.out.println(
                    "No hay préstamos vencidos."
            );
            return;
        }

        LocalDate hoy = LocalDate.now();

        for (Prestamo prestamo : prestamos) {

            int dias =
                    prestamoService.calcularDiasAtraso(
                            prestamo.getFechaVencimiento(),
                            hoy
                    );

            double multaEstimada =
                    prestamoService.calcularMultaPendiente(
                            prestamo
                    );

            System.out.printf(
                    "Préstamo #%d | Usuario: %d | "
                            + "Material: %s | Vencimiento: %s | "
                            + "Días de atraso: %d | Multa estimada: $%.2f%n",
                    prestamo.getId(),
                    prestamo.getUsuario().getId(),
                    prestamo.getMaterial().getTitulo(),
                    prestamo.getFechaVencimiento(),
                    dias,
                    multaEstimada
            );
        }
    }

    private static void verHistorialPorUsuario()
            throws SQLException {

        System.out.println(
                "--- HISTORIAL POR USUARIO ---"
        );

        int usuarioId = leerEnteroPositivo(
                "ID del usuario: "
        );

        if (usuarioDAO.buscarPorId(usuarioId) == null) {
            System.out.println(
                    "No existe ese usuario."
            );
            return;
        }

        List<Prestamo> prestamos =
                prestamoService.listarPrestamosPorUsuario(
                        usuarioId
                );

        if (prestamos.isEmpty()) {
            System.out.println(
                    "El usuario no tiene préstamos."
            );
            return;
        }

        for (Prestamo prestamo : prestamos) {
            mostrarPrestamo(prestamo);
        }
    }

    private static void verHistorialPorMaterial()
            throws SQLException {

        System.out.println(
                "--- HISTORIAL POR MATERIAL ---"
        );

        String codigo =
                leerTextoNoVacio(
                        "Código del material: "
                ).toUpperCase();

        if (materialDAO.buscarPorCodigo(codigo) == null) {
            System.out.println(
                    "No existe ese material."
            );
            return;
        }

        List<Prestamo> prestamos =
                prestamoService.listarHistorialPorMaterial(
                        codigo
                );

        if (prestamos.isEmpty()) {
            System.out.println(
                    "El material no tiene historial de préstamos."
            );
            return;
        }

        for (Prestamo prestamo : prestamos) {
            mostrarPrestamo(prestamo);
        }
    }

    private static void verMultasPorUsuario()
            throws SQLException {

        System.out.println(
                "--- MULTAS POR USUARIO ---"
        );

        int usuarioId = leerEnteroPositivo(
                "ID del usuario: "
        );

        Usuario usuario =
                usuarioDAO.buscarPorId(usuarioId);

        if (usuario == null) {
            System.out.println(
                    "No existe ese usuario."
            );
            return;
        }

        List<Multa> multas =
                multaDAO.listarPorUsuario(usuarioId);

        double pendiente =
                multaService.calcularMontoAcumulado(
                        usuario
                );

        System.out.printf(
                "Deuda pendiente: $%.2f%n",
                pendiente
        );

        if (multas.isEmpty()) {
            System.out.println(
                    "No hay multas registradas."
            );
            return;
        }

        for (Multa multa : multas) {
            mostrarMulta(multa);
        }
    }

    private static void verTodasLasMultas()
            throws SQLException {

        System.out.println(
                "--- TODAS LAS MULTAS ---"
        );

        List<Multa> multas =
                multaDAO.listarTodas();

        if (multas.isEmpty()) {
            System.out.println(
                    "No hay multas registradas."
            );
            return;
        }

        for (Multa multa : multas) {
            mostrarMulta(multa);
        }
    }

    private static void pagarMulta()
            throws SQLException {

        System.out.println(
                "--- REGISTRAR PAGO DE MULTA ---"
        );

        int multaId = leerEnteroPositivo(
                "ID de la multa: "
        );

        multaDAO.marcarComoPagada(multaId);

        System.out.println(
                "Pago registrado correctamente."
        );
    }

    private static void mostrarPrestamo(
            Prestamo prestamo) {

        System.out.printf(
                "Préstamo #%d | Usuario: %d (%s) | "
                        + "Material: %s [%s] | Inicio: %s | "
                        + "Vencimiento: %s | Devolución: %s%n",
                prestamo.getId(),
                prestamo.getUsuario().getId(),
                prestamo.getUsuario().getTipoUsuario(),
                prestamo.getMaterial().getTitulo(),
                prestamo.getMaterial().getCodigo(),
                prestamo.getFechaInicio(),
                prestamo.getFechaVencimiento(),
                prestamo.getFechaDevolucion() == null
                        ? "PENDIENTE"
                        : prestamo.getFechaDevolucion()
        );
    }

    private static void mostrarMulta(
            Multa multa) {

        System.out.printf(
                "Multa #%d | Préstamo #%d | "
                        + "Usuario: %d | Días atraso: %d | "
                        + "Monto: $%.2f | Estado: %s%n",
                multa.getId(),
                multa.getPrestamo().getId(),
                multa.getPrestamo().getUsuario().getId(),
                multa.getDiasAtraso(),
                multa.getMonto(),
                multa.isPagada()
                        ? "PAGADA"
                        : "PENDIENTE"
        );
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private static String leerTextoNoVacio(
            String mensaje) {

        while (true) {
            String texto = leerTexto(mensaje);

            if (!texto.isBlank()) {
                return texto;
            }

            System.out.println(
                    "El valor no puede quedar vacío."
            );
        }
    }

    private static int leerEntero(String mensaje) {

        while (true) {

            try {
                return Integer.parseInt(
                        leerTexto(mensaje)
                );
            } catch (NumberFormatException e) {
                System.out.println(
                        "Ingrese un número entero válido."
                );
            }
        }
    }

    private static int leerEnteroPositivo(
            String mensaje) {

        while (true) {

            int numero =
                    leerEntero(mensaje);

            if (numero > 0) {
                return numero;
            }

            System.out.println(
                    "El número debe ser mayor que cero."
            );
        }
    }
}
