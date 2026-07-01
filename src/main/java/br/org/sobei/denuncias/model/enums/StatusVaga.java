package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum StatusVaga {
    ATIVO("ativo"),
    EM_SELECAO("em_selecao"),
    FECHADO("fechado");

    private final String value;

    StatusVaga(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StatusVaga fromValue(String value) {
        return Stream.of(StatusVaga.values())
                .filter(s -> s.getValue().equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status de vaga invalido: " + value));
    }
}
