package com.itu.biblioteca.model;

public class Revista extends MaterialBibliografico {

    public Revista(String codigo, String titulo, int cantidadTotal) {
        super(codigo, titulo, cantidadTotal);
    }

    @Override
    public String getTipoMaterial() {
        return "REVISTA";
    }

    @Override
    public boolean esCompatibleCon(Usuario usuario) {
        return "DOCENTE".equals(usuario.getTipoUsuario());
    }
}
