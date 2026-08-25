package br.org.sobei.denuncias.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    @ParameterizedTest
    @DisplayName("Deve validar CPFs numéricos tradicionais válidos")
    @ValueSource(strings = {
            "11144477735",
            "111.444.777-35",
            "12345678909",
            "123.456.789-09",
            "52998224725",
            "529.982.247-25"
    })
    void deveValidarCpfsNumericosValidos(String cpf) {
        assertTrue(CpfValidator.isValido(cpf), "CPF deveria ser válido: " + cpf);
    }

    @ParameterizedTest
    @DisplayName("Deve validar CPFs alfanuméricos válidos conforme regra da Receita Federal")
    @ValueSource(strings = {
            "12A45B78C52",     // 12A45B78C com DVs 52
            "12A.45B.78C-52",
            "ABC12345602",     // ABC123456 com DVs 02
            "ABC.123.456-02"
    })
    void deveValidarCpfsAlfanumericosValidos(String cpf) {
        assertTrue(CpfValidator.isValido(cpf), "CPF alfanumérico deveria ser válido: " + cpf);
    }

    @ParameterizedTest
    @DisplayName("Deve rejeitar CPFs com dígitos repetidos inválidos")
    @ValueSource(strings = {
            "00000000000",
            "000.000.000-00",
            "11111111111",
            "111.111.111-11",
            "99999999999",
            "AAAAAAAAAAA"
    })
    void deveRejeitarCpfsRepetidos(String cpf) {
        assertFalse(CpfValidator.isValido(cpf), "CPF com dígitos repetidos não deveria ser válido: " + cpf);
    }

    @ParameterizedTest
    @DisplayName("Deve rejeitar CPFs com dígitos verificadores incorretos")
    @ValueSource(strings = {
            "11144477734", // DV2 errado
            "11144477725", // DV1 errado
            "12345678900", // DVs errados
            "123.456.789-10"
    })
    void deveRejeitarCpfsComDigitoIncorreto(String cpf) {
        assertFalse(CpfValidator.isValido(cpf), "CPF com DV incorreto deveria ser rejeitado: " + cpf);
    }

    @ParameterizedTest
    @DisplayName("Deve rejeitar valores nulos, vazios ou de tamanho inválido")
    @ValueSource(strings = {
            "",
            "   ",
            "123",
            "1234567890",
            "123456789012"
    })
    void deveRejeitarTamanhoInvalido(String cpf) {
        assertFalse(CpfValidator.isValido(cpf));
    }

    @Test
    @DisplayName("Deve formatar corretamente CPFs")
    void deveFormatarCpf() {
        assertEquals("111.444.777-35", CpfValidator.formatar("11144477735"));
        assertEquals("12A.45B.78C-16", CpfValidator.formatar("12a45b78c16"));
        assertEquals("12A.45B.78C-16", CpfValidator.formatar("12A.45B.78C-16"));
    }
}
