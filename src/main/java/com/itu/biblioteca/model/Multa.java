package com.itu.biblioteca.model;

public class Multa {

    private int id;
    private Prestamo prestamo;
    private int diasAtraso;
    private double monto;
    private boolean pagada;

    public Multa(
            int id,
            Prestamo prestamo,
            int diasAtraso,
            double monto) {

        this.id = id;
        this.prestamo = prestamo;
        this.diasAtraso = diasAtraso;
        this.monto = monto;
        this.pagada = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public double getMonto() {
        return monto;
    }

    public boolean isPagada() {
        return pagada;
    }

    public void setPagada(boolean pagada) {
        this.pagada = pagada;
    }

    public void marcarComoPagada() {
        this.pagada = true;
    }
}
