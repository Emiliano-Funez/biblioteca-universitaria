package com.itu.biblioteca.dao;

import com.itu.biblioteca.database.ConexionBD;
import com.itu.biblioteca.model.Libro;
import com.itu.biblioteca.model.MaterialBibliografico;
import com.itu.biblioteca.model.Revista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {

    public void guardar(MaterialBibliografico material)
            throws SQLException {

        String sql =
                "INSERT INTO materiales "
                + "(codigo, titulo, tipo, cantidad_total, cantidad_disponible) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setString(1, material.getCodigo());
            statement.setString(2, material.getTitulo());
            statement.setString(3, material.getTipoMaterial());
            statement.setInt(4, material.getCantidadTotal());
            statement.setInt(5, material.getCantidadDisponible());

            statement.executeUpdate();
        }
    }

    public MaterialBibliografico buscarPorCodigo(String codigo)
            throws SQLException {

        String sql =
                "SELECT codigo, titulo, tipo, cantidad_total, cantidad_disponible "
                + "FROM materiales WHERE codigo = ?";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setString(1, codigo);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMaterial(rs);
                }
            }
        }

        return null;
    }

    public List<MaterialBibliografico> listar()
            throws SQLException {

        String sql =
                "SELECT codigo, titulo, tipo, cantidad_total, cantidad_disponible "
                + "FROM materiales ORDER BY codigo";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            List<MaterialBibliografico> materiales = new ArrayList<>();

            while (rs.next()) {
                materiales.add(mapResultSetToMaterial(rs));
            }

            return materiales;
        }
    }

    public List<MaterialBibliografico> listarDisponibles()
            throws SQLException {

        String sql =
                "SELECT codigo, titulo, tipo, cantidad_total, cantidad_disponible "
                + "FROM materiales "
                + "WHERE cantidad_disponible > 0 "
                + "ORDER BY codigo";

        return listarPorConsulta(sql);
    }

    public List<MaterialBibliografico> listarNoDisponibles()
            throws SQLException {

        String sql =
                "SELECT codigo, titulo, tipo, cantidad_total, cantidad_disponible "
                + "FROM materiales "
                + "WHERE cantidad_disponible = 0 "
                + "ORDER BY codigo";

        return listarPorConsulta(sql);
    }

    private List<MaterialBibliografico> listarPorConsulta(String sql)
            throws SQLException {

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            List<MaterialBibliografico> materiales = new ArrayList<>();

            while (rs.next()) {
                materiales.add(mapResultSetToMaterial(rs));
            }

            return materiales;
        }
    }

    private MaterialBibliografico mapResultSetToMaterial(
            ResultSet rs) throws SQLException {

        String codigo = rs.getString("codigo");
        String titulo = rs.getString("titulo");
        String tipo = rs.getString("tipo");
        int cantidadTotal = rs.getInt("cantidad_total");
        int cantidadDisponible = rs.getInt("cantidad_disponible");

        MaterialBibliografico material;

        if ("LIBRO".equalsIgnoreCase(tipo)) {
            material = new Libro(codigo, titulo, cantidadTotal);
        } else if ("REVISTA".equalsIgnoreCase(tipo)) {
            material = new Revista(codigo, titulo, cantidadTotal);
        } else {
            throw new SQLException(
                    "Tipo de material desconocido en la base de datos: "
                    + tipo
            );
        }

        material.setCantidadDisponible(cantidadDisponible);
        return material;
    }
}
