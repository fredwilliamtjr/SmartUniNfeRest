package br.com.smartfull.rest;

public class Resposta {

    private final String conteudo;

    public Resposta(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getConteudo() {
        return conteudo;
    }
}
