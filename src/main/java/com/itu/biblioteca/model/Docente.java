package com.itu.biblioteca.model;

public class Docente extends Usuario {

    public Docente(int id, String nombre, String apellido) {
        super(id, nombre, apellido);
    }

    @Override
    public int getLimitePrestamos() {
        return 5;
    }

    @Override
    public int getDiasPrestamo() {
        return 14;
    }

    @Override
    public double getMultaPorDia() {
        return 300.0;
    }

    @Override
    public String getTipoUsuario() {
        return "DOCENTE";
    }
}
