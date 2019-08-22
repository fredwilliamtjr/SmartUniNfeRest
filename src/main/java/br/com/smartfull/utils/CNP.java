package br.com.smartfull.utils;

import java.io.Serializable;

/**
 *
 * @author Fred William Torno Junior - fredwilliam@gmail.com -
 * fredwilliam@outlook.com - www.fwtj.com.br - (19) 98215-5340
 */

public class CNP implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int[] pesoCPF = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] pesoCNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private static int calcularDigito(String str, int[] peso) {
        int soma = 0;
        for (int indice = str.length() - 1, digito; indice >= 0; indice--) {
            digito = Integer.parseInt(str.substring(indice, indice + 1));
            soma += digito * peso[peso.length - str.length() + indice];
        }
        soma = 11 - soma % 11;
        return soma > 9 ? 0 : soma;
    }

    public static boolean isValidCPF(String cpf) {
        cpf = cpf.replace(".", "").replace("-", "");
        if ((cpf == null) || (cpf.length() != 11) || (cpf.equals("11111111111")) || (cpf.equals("22222222222")) || (cpf.equals("33333333333")) || (cpf.equals("44444444444") || (cpf.equals("55555555555")) || (cpf.equals("66666666666")) || (cpf.equals("77777777777")) || (cpf.equals("88888888888")) || (cpf.equals("99999999999")))) {
            return false;
        }
        Integer digito1 = calcularDigito(cpf.substring(0, 9), pesoCPF);
        Integer digito2 = calcularDigito(cpf.substring(0, 9) + digito1, pesoCPF);
        return cpf.equals(cpf.substring(0, 9) + digito1.toString() + digito2.toString());
    }

    public static boolean isValidCNPJ(String cnpj) {
        cnpj = cnpj.replace(".", "").replace("/", "").replace("-", "");
        if ((cnpj == null) || (cnpj.length() != 14)) {
            return false;
        }

        Integer digito1 = calcularDigito(cnpj.substring(0, 12), pesoCNPJ);
        Integer digito2 = calcularDigito(cnpj.substring(0, 12) + digito1, pesoCNPJ);
        return cnpj.equals(cnpj.substring(0, 12) + digito1.toString() + digito2.toString());
    }

    public static String removeMascara(String texto) {
        return texto.replace(".", "").replace("/", "").replace("-", "").replace("_", "");
    }

    /*
     * realiza a formatação do valor de acordo com a mascara enviada
     */
    private static String formatar(String valor, String mascara) {

        String dado = "";
        // remove caracteres nao numericos
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (Character.isDigit(c)) {
                dado += c;
            }
        }

        int indMascara = mascara.length();
        int indCampo = dado.length();

        for (; indCampo > 0 && indMascara > 0;) {
            if (mascara.charAt(--indMascara) == '#') {
                indCampo--;
            }
        }

        String saida = "";
        for (; indMascara < mascara.length(); indMascara++) {
            saida += ((mascara.charAt(indMascara) == '#') ? dado.charAt(indCampo++) : mascara.charAt(indMascara));
        }
        return saida;
    }

    public static String formatarCpf(String cpf) {
        while (cpf.length() < 11) {
            cpf = "0" + cpf;
        }
        return formatar(cpf, "###.###.###-##");
    }

    public static String formatarCnpj(String cnpj) {
        while (cnpj.length() < 14) {
            cnpj = "0" + cnpj;
        }
        return formatar(cnpj, "##.###.###/####-##");
    }

}
