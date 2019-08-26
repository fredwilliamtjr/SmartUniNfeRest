package br.com.smartfull.rest;

public class Resposta {

    private Integer httpStatusCode;
    private String conteudo;

    public Resposta(String conteudo, Integer httpStatusCode) {
        this.conteudo = conteudo;
        this.httpStatusCode = httpStatusCode;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getConteudo() {
        return conteudo;
    }

    @Override
    public String toString() {
        return "Resposta{" +
                "httpStatusCode=" + httpStatusCode +
                ", conteudo='" + conteudo + '\'' +
                '}';
    }
}
