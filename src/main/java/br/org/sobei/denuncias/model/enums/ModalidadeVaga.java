package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum ModalidadeVaga {
    PRESENCIAL("presencial"),
    HIBRIDO("hibrido"),
    REMOTO("remoto");

    private final String value;

    ModalidadeVaga(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ModalidadeVaga fromValue(String value) {
        return Stream.of(ModalidadeVaga.values())
                .filter(s -> s.getValue().equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Modalidade invalida: " + value));
    }
}
