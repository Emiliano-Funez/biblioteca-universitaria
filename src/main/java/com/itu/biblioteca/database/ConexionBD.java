package com.itu.biblioteca.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/biblioteca_universitaria"
            + "?useSSL=false&serverTimezone=America/Argentina/Mendoza"
            + "&allowPublicKeyRetrieval=true";

    private static final String USUARIO = "root";

    /*
     * La contraseña se obtiene desde una variable de entorno.
     * Así no se publica la clave de MySQL en GitHub.
     */
    private static final String PASSWORD =
            System.getenv("BIBLIOTECA_DB_PASSWORD");

    private ConexionBD() {
    }

    public static Connection conectar() throws SQLException {

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "No está configurada la variable de entorno "
                            + "BIBLIOTECA_DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(
                URL,
                USUARIO,
                PASSWORD
        );
    }
}
