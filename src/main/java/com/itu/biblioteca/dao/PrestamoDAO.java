package com.itu.biblioteca.dao;

import com.itu.biblioteca.database.ConexionBD;
import com.itu.biblioteca.model.Docente;
import com.itu.biblioteca.model.Estudiante;
import com.itu.biblioteca.model.Libro;
import com.itu.biblioteca.model.MaterialBibliografico;
import com.itu.biblioteca.model.Prestamo;
import com.itu.biblioteca.model.Revista;
import com.itu.biblioteca.model.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private static final String SELECT_COMPLETO =
            "SELECT p.id AS prestamo_id, "
            + "p.usuario_id, p.material_codigo, "
            + "p.fecha_inicio, p.fecha_vencimiento, p.fecha_devolucion, "
            + "u.nombre AS usuario_nombre, "
            + "u.apellido AS usuario_apellido, "
            + "u.tipo AS tipo_usuario, "
            + "u.activo AS usuario_activo, "
            + "m.titulo AS material_titulo, "
            + "m.tipo AS tipo_material, "
            + "m.cantidad_total, "
            + "m.cantidad_disponible "
            + "FROM prestamos p "
            + "JOIN usuarios u ON p.usuario_id = u.id "
            + "JOIN materiales m ON p.material_codigo = m.codigo ";

    public void guardar(Prestamo prestamo)
            throws SQLException {

        String sqlPrestamo =
                "INSERT INTO prestamos "
                + "(usuario_id, material_codigo, fecha_inicio, "
                + "fecha_vencimiento, fecha_devolucion) "
                + "VALUES (?, ?, ?, ?, ?)";

        String sqlMaterial =
                "UPDATE materiales "
                + "SET cantidad_disponible = ? "
                + "WHERE codigo = ?";

        try (Connection conexion = ConexionBD.conectar()) {

            try {
                conexion.setAutoCommit(false);

                try (
                        PreparedStatement statement =
                                conexion.prepareStatement(
                                        sqlPrestamo,
                                        Statement.RETURN_GENERATED_KEYS
                                )
                ) {
                    statement.setInt(
                            1,
                            prestamo.getUsuario().getId()
                    );

                    statement.setString(
                            2,
                            prestamo.getMaterial().getCodigo()
                    );

                    statement.setDate(
                            3,
                            Date.valueOf(prestamo.getFechaInicio())
                    );

                    statement.setDate(
                            4,
                            Date.valueOf(prestamo.getFechaVencimiento())
                    );

                    statement.setNull(
                            5,
                            java.sql.Types.DATE
                    );

                    int filas = statement.executeUpdate();

                    if (filas != 1) {
                        throw new SQLException(
                                "No se pudo insertar el préstamo."
                        );
                    }

                    try (ResultSet generatedKeys =
                                 statement.getGeneratedKeys()) {

                        if (!generatedKeys.next()) {
                            throw new SQLException(
                                    "No se pudo obtener el ID del préstamo."
                            );
                        }

                        prestamo.setId(
                                generatedKeys.getInt(1)
                        );
                    }
                }

                try (
                        PreparedStatement statement =
                                conexion.prepareStatement(sqlMaterial)
                ) {
                    statement.setInt(
                            1,
                            prestamo.getMaterial().getCantidadDisponible()
                    );

                    statement.setString(
                            2,
                            prestamo.getMaterial().getCodigo()
                    );

                    int filas = statement.executeUpdate();

                    if (filas != 1) {
                        throw new SQLException(
                                "No se pudo actualizar la disponibilidad."
                        );
                    }
                }

                conexion.commit();

            } catch (SQLException e) {
                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void registrarDevolucion(Prestamo prestamo)
            throws SQLException {

        registrarDevolucion(prestamo, null);
    }

    public void registrarDevolucion(
            Prestamo prestamo,
            com.itu.biblioteca.model.Multa multa)
            throws SQLException {

        String sqlPrestamo =
                "UPDATE prestamos "
                + "SET fecha_devolucion = ? "
                + "WHERE id = ? "
                + "AND fecha_devolucion IS NULL";

        String sqlMaterial =
                "UPDATE materiales "
                + "SET cantidad_disponible = ? "
                + "WHERE codigo = ?";

        String sqlMulta =
                "INSERT INTO multas "
                + "(prestamo_id, dias_atraso, monto, pagada) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar()) {

            try {
                conexion.setAutoCommit(false);

                try (
                        PreparedStatement statement =
                                conexion.prepareStatement(sqlPrestamo)
                ) {
                    statement.setDate(
                            1,
                            Date.valueOf(prestamo.getFechaDevolucion())
                    );

                    statement.setInt(
                            2,
                            prestamo.getId()
                    );

                    int filas = statement.executeUpdate();

                    if (filas != 1) {
                        throw new SQLException(
                                "El préstamo ya fue devuelto o no existe."
                        );
                    }
                }

                try (
                        PreparedStatement statement =
                                conexion.prepareStatement(sqlMaterial)
                ) {
                    statement.setInt(
                            1,
                            prestamo.getMaterial().getCantidadDisponible()
                    );

                    statement.setString(
                            2,
                            prestamo.getMaterial().getCodigo()
                    );

                    int filas = statement.executeUpdate();

                    if (filas != 1) {
                        throw new SQLException(
                                "No se pudo actualizar la disponibilidad."
                        );
                    }
                }

                if (multa != null) {
                    try (
                            PreparedStatement statement =
                                    conexion.prepareStatement(
                                            sqlMulta,
                                            Statement.RETURN_GENERATED_KEYS
                                    )
                    ) {
                        statement.setInt(
                                1,
                                multa.getPrestamo().getId()
                        );
                        statement.setInt(
                                2,
                                multa.getDiasAtraso()
                        );
                        statement.setDouble(
                                3,
                                multa.getMonto()
                        );
                        statement.setBoolean(
                                4,
                                multa.isPagada()
                        );

                        statement.executeUpdate();

                        try (ResultSet generatedKeys =
                                     statement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                multa.setId(
                                        generatedKeys.getInt(1)
                                );
                            }
                        }
                    }
                }

                conexion.commit();

            } catch (SQLException e) {
                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public Prestamo buscarPorId(int id)
            throws SQLException {

        String sql =
                SELECT_COMPLETO
                + "WHERE p.id = ?";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapPrestamo(rs);
                }
            }
        }

        return null;
    }

    public int contarPrestamosActivosPorUsuario(int usuarioId)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) AS total "
                + "FROM prestamos "
                + "WHERE usuario_id = ? "
                + "AND fecha_devolucion IS NULL";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setInt(1, usuarioId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    public List<Prestamo> listarActivosPorUsuario(int usuarioId)
            throws SQLException {

        String sql =
                SELECT_COMPLETO
                + "WHERE p.usuario_id = ? "
                + "AND p.fecha_devolucion IS NULL "
                + "ORDER BY p.id";

        return listarPorConsulta(sql, usuarioId);
    }

    public List<Prestamo> listarPrestamosPorUsuario(int usuarioId)
            throws SQLException {

        String sql =
                SELECT_COMPLETO
                + "WHERE p.usuario_id = ? "
                + "ORDER BY p.id";

        return listarPorConsulta(sql, usuarioId);
    }

    public List<Prestamo> listarHistorialPorMaterial(
            String codigo) throws SQLException {

        String sql =
                SELECT_COMPLETO
                + "WHERE p.material_codigo = ? "
                + "ORDER BY p.id";

        return listarPorConsulta(sql, codigo);
    }

    public List<Prestamo> listarVencidos() throws SQLException {

        String sql =
                SELECT_COMPLETO
                + "WHERE p.fecha_vencimiento < CURRENT_DATE() "
                + "AND p.fecha_devolucion IS NULL "
                + "ORDER BY p.fecha_vencimiento";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            List<Prestamo> prestamos = new ArrayList<>();

            while (rs.next()) {
                prestamos.add(mapPrestamo(rs));
            }

            return prestamos;
        }
    }

    private List<Prestamo> listarPorConsulta(
            String sql,
            Object parametro) throws SQLException {

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            if (parametro instanceof Integer) {
                statement.setInt(1, (Integer) parametro);
            } else {
                statement.setString(1, parametro.toString());
            }

            try (ResultSet rs = statement.executeQuery()) {
                List<Prestamo> prestamos = new ArrayList<>();

                while (rs.next()) {
                    prestamos.add(mapPrestamo(rs));
                }

                return prestamos;
            }
        }
    }

    private Prestamo mapPrestamo(ResultSet rs)
            throws SQLException {

        Usuario usuario;

        String tipoUsuario = rs.getString("tipo_usuario");

        if ("ESTUDIANTE".equalsIgnoreCase(tipoUsuario)) {
            usuario = new Estudiante(
                    rs.getInt("usuario_id"),
                    rs.getString("usuario_nombre"),
                    rs.getString("usuario_apellido")
            );
        } else if ("DOCENTE".equalsIgnoreCase(tipoUsuario)) {
            usuario = new Docente(
                    rs.getInt("usuario_id"),
                    rs.getString("usuario_nombre"),
                    rs.getString("usuario_apellido")
            );
        } else {
            throw new SQLException(
                    "Tipo de usuario desconocido: " + tipoUsuario
            );
        }

        usuario.setActivo(
                rs.getBoolean("usuario_activo")
        );

        MaterialBibliografico material;

        String tipoMaterial = rs.getString("tipo_material");

        if ("LIBRO".equalsIgnoreCase(tipoMaterial)) {
            material = new Libro(
                    rs.getString("material_codigo"),
                    rs.getString("material_titulo"),
                    rs.getInt("cantidad_total")
            );
        } else if ("REVISTA".equalsIgnoreCase(tipoMaterial)) {
            material = new Revista(
                    rs.getString("material_codigo"),
                    rs.getString("material_titulo"),
                    rs.getInt("cantidad_total")
            );
        } else {
            throw new SQLException(
                    "Tipo de material desconocido: "
                    + tipoMaterial
            );
        }

        material.setCantidadDisponible(
                rs.getInt("cantidad_disponible")
        );

        LocalDate fechaDevolucion = null;

        if (rs.getDate("fecha_devolucion") != null) {
            fechaDevolucion =
                    rs.getDate("fecha_devolucion").toLocalDate();
        }

        return new Prestamo(
                rs.getInt("prestamo_id"),
                usuario,
                material,
                rs.getDate("fecha_inicio").toLocalDate(),
                rs.getDate("fecha_vencimiento").toLocalDate(),
                fechaDevolucion
        );
    }
}
