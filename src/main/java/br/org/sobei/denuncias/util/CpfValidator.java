package br.org.sobei.denuncias.util;

/**
 * Validador e formatador robusto de CPF para o Portal SOBEI.
 * 
 * Suporta tanto o CPF Tradicional Numérico (11 dígitos) quanto o
 * CPF Alfanumérico da Receita Federal do Brasil (IN RFB nº 2.229/2024),
 * onde os 9 primeiros caracteres podem ser alfanuméricos (0-9 e A-Z)
 * e os 2 últimos caracteres são os dígitos verificadores numéricos (0-9)
 * calculados via algoritmo oficial de Módulo 11 da Receita Federal (ASCII - 48).
 */
public final class CpfValidator {

    private CpfValidator() {
        // Classe utilitária, não instanciar
    }

    /**
     * Valida um CPF (numérico ou alfanumérico) conforme o algoritmo oficial
     * de Módulo 11 da Receita Federal do Brasil.
     *
     * @param cpf String contendo o CPF formatado ou desformatado
     * @return true se o CPF for válido e seus dígitos verificadores conferirem; false caso contrário.
     */
    public static boolean isValido(String cpf) {
        if (cpf == null || cpf.trim().isBlank()) {
            return false;
        }

        String limpo = desformatar(cpf);

        // Deve conter exatamente 11 caracteres
        if (limpo.length() != 11) {
            return false;
        }

        // Rejeita sequências com todos os caracteres iguais (ex: 00000000000, 11111111111, AAAAAAAAAAA)
        if (todosCaracteresIguais(limpo)) {
            return false;
        }

        // As primeiras 9 posições devem ser alfanuméricas [0-9A-Z]
        for (int i = 0; i < 9; i++) {
            char c = limpo.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z'))) {
                return false;
            }
        }

        // As duas últimas posições (dígitos verificadores) devem ser estritamente numéricas [0-9]
        char dv1 = limpo.charAt(9);
        char dv2 = limpo.charAt(10);
        if (dv1 < '0' || dv1 > '9' || dv2 < '0' || dv2 > '9') {
            return false;
        }

        // Cálculo do 1º Dígito Verificador (DV1)
        int soma1 = 0;
        for (int i = 0; i < 9; i++) {
            int valor = getValorCaractere(limpo.charAt(i));
            soma1 += valor * (10 - i);
        }
        int resto1 = soma1 % 11;
        int digitoEsperado1 = (resto1 < 2) ? 0 : (11 - resto1);
        int digitoInformado1 = dv1 - '0';

        if (digitoInformado1 != digitoEsperado1) {
            return false;
        }

        // Cálculo do 2º Dígito Verificador (DV2)
        int soma2 = 0;
        for (int i = 0; i < 9; i++) {
            int valor = getValorCaractere(limpo.charAt(i));
            soma2 += valor * (11 - i);
        }
        soma2 += digitoEsperado1 * 2;
        int resto2 = soma2 % 11;
        int digitoEsperado2 = (resto2 < 2) ? 0 : (11 - resto2);
        int digitoInformado2 = dv2 - '0';

        return digitoInformado2 == digitoEsperado2;
    }

    /**
     * Retorna o valor numérico do caractere de acordo com a especificação
     * oficial da Receita Federal para cálculo do Módulo 11 (ASCII - 48).
     * '0'-'9' => 0..9
     * 'A'-'Z' => 17..42
     */
    private static int getValorCaractere(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'Z') {
            return (int) c - 48; // 'A' (65 - 48 = 17)
        }
        throw new IllegalArgumentException("Caractere inválido no CPF: " + c);
    }

    private static boolean todosCaracteresIguais(String s) {
        char primeiro = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != primeiro) {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove formatação, espaços, pontos e traços, retornando apenas caracteres alfanuméricos em caixa alta.
     */
    public static String desformatar(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    /**
     * Formata um CPF para a máscara oficial: XXX.XXX.XXX-XX
     * Caso tenha tamanho diferente de 11, retorna a string desformatada limpa.
     */
    public static String formatar(String cpf) {
        String limpo = desformatar(cpf);
        if (limpo.length() != 11) {
            return limpo;
        }
        return String.format("%s.%s.%s-%s",
                limpo.substring(0, 3),
                limpo.substring(3, 6),
                limpo.substring(6, 9),
                limpo.substring(9, 11));
    }
}
