package com.itu.biblioteca.model;

import java.time.LocalDate;

public class Prestamo {

    private int id;
    private Usuario usuario;
    private MaterialBibliografico material;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private LocalDate fechaDevolucion;

    public Prestamo(
            int id,
            Usuario usuario,
            MaterialBibliografico material,
            LocalDate fechaInicio) {

        this(
                id,
                usuario,
                material,
                fechaInicio,
                fechaInicio.plusDays(usuario.getDiasPrestamo()),
                null
        );
    }

    public Prestamo(
            int id,
            Usuario usuario,
            MaterialBibliografico material,
            LocalDate fechaInicio,
            LocalDate fechaVencimiento,
            LocalDate fechaDevolucion) {

        this.id = id;
        this.usuario = usuario;
        this.material = material;
        this.fechaInicio = fechaInicio;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaDevolucion = fechaDevolucion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public MaterialBibliografico getMaterial() {
        return material;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public boolean estaActivo() {
        return fechaDevolucion == null;
    }
}
