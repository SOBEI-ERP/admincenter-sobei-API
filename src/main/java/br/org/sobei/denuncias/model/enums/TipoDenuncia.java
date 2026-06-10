package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.stream.Stream;

@Getter
public enum TipoDenuncia {
    ANONIMA("anonima"),
    IDENTIFICADA("identificada");

    private final String value;

    TipoDenuncia(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TipoDenuncia fromValue(String value) {
        return Stream.of(TipoDenuncia.values())
                .filter(t -> t.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de denuncia invalido: " + value));
    }
}