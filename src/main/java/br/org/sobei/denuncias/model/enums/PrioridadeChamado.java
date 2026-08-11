package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.stream.Stream;

@Getter
public enum PrioridadeChamado {
    BAIXA("baixa"),
    MEDIA("media"),
    ALTA("alta"),
    URGENTE("urgente");

    private final String value;

    PrioridadeChamado(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PrioridadeChamado fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        return Stream.of(PrioridadeChamado.values())
                .filter(s -> s.getValue().equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Prioridade de chamado inválida: " + value));
    }
}
