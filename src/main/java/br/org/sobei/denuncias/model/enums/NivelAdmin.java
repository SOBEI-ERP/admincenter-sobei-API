package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum NivelAdmin {
    admin("admin"),
    suporte("suporte");

    private final String value;

    NivelAdmin(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NivelAdmin fromValue(String value) {
        return Stream.of(NivelAdmin.values())
                .filter(s -> s.getValue().equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nivel invalido: " + value));
    }
}

