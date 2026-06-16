package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.stream.Stream;

@Getter
public enum PrioridadeDenuncia {
    NEUTRA("neutra"),
    BAIXA("baixa"),
    MEDIA("media"),
    ALTA("alta");

    private final String value;

    PrioridadeDenuncia(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PrioridadeDenuncia fromValue(String value) {
        if (value == null) return null;
        return Stream.of(PrioridadeDenuncia.values())
                .filter(t -> t.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Prioridade inválida: " + value));
    }
}
