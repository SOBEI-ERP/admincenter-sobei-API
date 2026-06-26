package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum TipoContrato {
    CLT("clt"),
    ESTAGIO("estagio"),
    PJ("pj"),
    TEMPORARIO("temporario"),
    JOVEM_APRENDIZ("jovem_aprendiz");

    private final String value;

    TipoContrato(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TipoContrato fromValue(String value) {
        return Stream.of(TipoContrato.values())
                .filter(s -> s.getValue().equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de contrato invalido: " + value));
    }
}
