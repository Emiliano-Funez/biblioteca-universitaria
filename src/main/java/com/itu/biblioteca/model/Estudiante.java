package com.itu.biblioteca.model;

public class Estudiante extends Usuario {

    public Estudiante(int id, String nombre, String apellido) {
        super(id, nombre, apellido);
    }

    @Override
    public int getLimitePrestamos() {
        return 3;
    }

    @Override
    public int getDiasPrestamo() {
        return 7;
    }

    @Override
    public double getMultaPorDia() {
        return 500.0;
    }

    @Override
    public String getTipoUsuario() {
        return "ESTUDIANTE";
    }
}
