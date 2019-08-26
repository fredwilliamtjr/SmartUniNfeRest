package br.com.smartfull.rest;

public class Resposta {

    private String httpStatusCode;
    private String conteudo;

    public Resposta() {
    }

    public Resposta(String httpStatusCode, String conteudo) {
        this.httpStatusCode = httpStatusCode;
        this.conteudo = conteudo;
    }

    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    @Override
    public String toString() {
        return "Resposta{" +
                "httpStatusCode=" + httpStatusCode +
                ", conteudo='" + conteudo + '\'' +
                '}';
    }
}