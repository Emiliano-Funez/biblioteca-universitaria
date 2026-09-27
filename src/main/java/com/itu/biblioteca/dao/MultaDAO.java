package com.itu.biblioteca.dao;

import com.itu.biblioteca.database.ConexionBD;
import com.itu.biblioteca.model.Multa;
import com.itu.biblioteca.model.Prestamo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MultaDAO {

    public void guardar(Multa multa) throws SQLException {

        String sql =
                "INSERT INTO multas "
                + "(prestamo_id, dias_atraso, monto, pagada) "
                + "VALUES (?, ?, ?, ?)";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement =
                        conexion.prepareStatement(
                                sql,
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

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    multa.setId(rs.getInt(1));
                }
            }
        }
    }

    public boolean existePorPrestamo(int prestamoId)
            throws SQLException {

        String sql =
                "SELECT id FROM multas WHERE prestamo_id = ?";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setInt(1, prestamoId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public double calcularMontoPendientePorUsuario(int usuarioId)
            throws SQLException {

        String sql =
                "SELECT COALESCE(SUM(m.monto), 0) AS total "
                + "FROM multas m "
                + "JOIN prestamos p ON m.prestamo_id = p.id "
                + "WHERE p.usuario_id = ? "
                + "AND m.pagada = FALSE";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement =
                        conexion.prepareStatement(sql)
        ) {
            statement.setInt(1, usuarioId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }

        return 0.0;
    }

    public List<Multa> listarPorUsuario(int usuarioId)
            throws SQLException {

        String sql =
                "SELECT m.id, m.prestamo_id, "
                + "m.dias_atraso, m.monto, m.pagada "
                + "FROM multas m "
                + "JOIN prestamos p ON m.prestamo_id = p.id "
                + "WHERE p.usuario_id = ? "
                + "ORDER BY m.id";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement =
                        conexion.prepareStatement(sql)
        ) {
            statement.setInt(1, usuarioId);

            try (ResultSet rs = statement.executeQuery()) {
                List<Multa> multas = new ArrayList<>();

                while (rs.next()) {
                    multas.add(mapMulta(rs));
                }

                return multas;
            }
        }
    }

    public List<Multa> listarTodas() throws SQLException {

        String sql =
                "SELECT id, prestamo_id, dias_atraso, monto, pagada "
                + "FROM multas ORDER BY id";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement =
                        conexion.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            List<Multa> multas = new ArrayList<>();

            while (rs.next()) {
                multas.add(mapMulta(rs));
            }

            return multas;
        }
    }

    public void marcarComoPagada(int multaId)
            throws SQLException {

        String sql =
                "UPDATE multas SET pagada = TRUE "
                + "WHERE id = ?";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement =
                        conexion.prepareStatement(sql)
        ) {
            statement.setInt(1, multaId);

            int filas = statement.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "No se encontró la multa."
                );
            }
        }
    }

    private Multa mapMulta(ResultSet rs)
            throws SQLException {

        int prestamoId = rs.getInt("prestamo_id");

        PrestamoDAO prestamoDAO = new PrestamoDAO();

        Prestamo prestamo =
                prestamoDAO.buscarPorId(prestamoId);

        if (prestamo == null) {
            throw new SQLException(
                    "El préstamo asociado a la multa no existe."
            );
        }

        Multa multa = new Multa(
                rs.getInt("id"),
                prestamo,
                rs.getInt("dias_atraso"),
                rs.getDouble("monto")
        );

        multa.setPagada(
                rs.getBoolean("pagada")
        );

        return multa;
    }
}
