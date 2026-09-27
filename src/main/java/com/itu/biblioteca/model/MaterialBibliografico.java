package com.itu.biblioteca.model;

public abstract class MaterialBibliografico {

    private String codigo;
    private String titulo;
    private int cantidadTotal;
    private int cantidadDisponible;

    protected MaterialBibliografico(
            String codigo,
            String titulo,
            int cantidadTotal) {

        if (cantidadTotal <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad total debe ser mayor que cero.");
        }

        this.codigo = codigo;
        this.titulo = titulo;
        this.cantidadTotal = cantidadTotal;
        this.cantidadDisponible = cantidadTotal;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getCantidadTotal() {
        return cantidadTotal;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void disminuirDisponibilidad() {
        if (cantidadDisponible <= 0) {
            throw new IllegalStateException(
                    "No hay unidades disponibles.");
        }
        cantidadDisponible--;
    }

    public void aumentarDisponibilidad() {
        if (cantidadDisponible < cantidadTotal) {
            cantidadDisponible++;
        }
    }

    public void setCantidadDisponible(int cantidadDisponible) {
        if (cantidadDisponible < 0 || cantidadDisponible > cantidadTotal) {
            throw new IllegalArgumentException(
                    "La cantidad disponible es inválida.");
        }
        this.cantidadDisponible = cantidadDisponible;
    }

    public abstract String getTipoMaterial();

    public abstract boolean esCompatibleCon(Usuario usuario);
}
