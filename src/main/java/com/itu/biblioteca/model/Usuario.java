package com.itu.biblioteca.model;

public abstract class Usuario {

    private int id;
    private String nombre;
    private String apellido;
    private boolean activo;

    protected Usuario(int id, String nombre, String apellido) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.activo = true;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public abstract int getLimitePrestamos();

    public abstract int getDiasPrestamo();

    public abstract double getMultaPorDia();

    public abstract String getTipoUsuario();
}
