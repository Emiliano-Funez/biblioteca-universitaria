package com.itu.biblioteca.dao;

import com.itu.biblioteca.database.ConexionBD;
import com.itu.biblioteca.model.Docente;
import com.itu.biblioteca.model.Estudiante;
import com.itu.biblioteca.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public void guardar(Usuario usuario) throws SQLException {

        String sql =
                "INSERT INTO usuarios "
                + "(id, nombre, apellido, tipo, activo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setInt(1, usuario.getId());
            statement.setString(2, usuario.getNombre());
            statement.setString(3, usuario.getApellido());
            statement.setString(4, usuario.getTipoUsuario());
            statement.setBoolean(5, usuario.isActivo());

            statement.executeUpdate();
        }
    }

    public Usuario buscarPorId(int id) throws SQLException {

        String sql =
                "SELECT id, nombre, apellido, tipo, activo "
                + "FROM usuarios WHERE id = ?";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }

        return null;
    }

    public List<Usuario> listarTodos() throws SQLException {

        String sql =
                "SELECT id, nombre, apellido, tipo, activo "
                + "FROM usuarios ORDER BY id";

        return ejecutarLista(sql, null);
    }

    public List<Usuario> listarPorEstado(boolean activo)
            throws SQLException {

        String sql =
                "SELECT id, nombre, apellido, tipo, activo "
                + "FROM usuarios WHERE activo = ? ORDER BY id";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setBoolean(1, activo);

            try (ResultSet rs = statement.executeQuery()) {
                List<Usuario> usuarios = new ArrayList<>();

                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }

                return usuarios;
            }
        }
    }

    public void actualizarDatos(Usuario usuario)
            throws SQLException {

        String sql =
                "UPDATE usuarios "
                + "SET nombre = ?, apellido = ? "
                + "WHERE id = ?";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getApellido());
            statement.setInt(3, usuario.getId());

            int filas = statement.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "No se encontró el usuario para actualizar."
                );
            }
        }
    }

    public void actualizarEstado(Usuario usuario)
            throws SQLException {

        String sql =
                "UPDATE usuarios SET activo = ? WHERE id = ?";

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            statement.setBoolean(1, usuario.isActivo());
            statement.setInt(2, usuario.getId());

            int filas = statement.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "No se encontró el usuario para actualizar."
                );
            }
        }
    }

    private List<Usuario> ejecutarLista(
            String sql,
            Boolean estado) throws SQLException {

        try (
                Connection conexion = ConexionBD.conectar();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {
            if (estado != null) {
                statement.setBoolean(1, estado);
            }

            try (ResultSet rs = statement.executeQuery()) {
                List<Usuario> usuarios = new ArrayList<>();

                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }

                return usuarios;
            }
        }
    }

    private Usuario mapResultSetToUsuario(ResultSet rs)
            throws SQLException {

        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        String tipo = rs.getString("tipo");
        boolean activo = rs.getBoolean("activo");

        Usuario usuario;

        if ("ESTUDIANTE".equalsIgnoreCase(tipo)) {
            usuario = new Estudiante(id, nombre, apellido);
        } else if ("DOCENTE".equalsIgnoreCase(tipo)) {
            usuario = new Docente(id, nombre, apellido);
        } else {
            throw new SQLException(
                    "Tipo de usuario desconocido en la base de datos: "
                    + tipo
            );
        }

        usuario.setActivo(activo);
        return usuario;
    }
}
