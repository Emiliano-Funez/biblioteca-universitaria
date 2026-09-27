package com.itu.biblioteca.model;

public class Libro extends MaterialBibliografico {

    public Libro(String codigo, String titulo, int cantidadTotal) {
        super(codigo, titulo, cantidadTotal);
    }

    @Override
    public String getTipoMaterial() {
        return "LIBRO";
    }

    @Override
    public boolean esCompatibleCon(Usuario usuario) {
        return "ESTUDIANTE".equals(usuario.getTipoUsuario())
                || "DOCENTE".equals(usuario.getTipoUsuario());
    }
}
