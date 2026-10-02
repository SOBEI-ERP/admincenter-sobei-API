package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.stream.Stream;

@Getter
public enum TipoConclusao {
    FINAL("final"),
    ARQUIVAMENTO("arquivamento");

    private final String value;

    TipoConclusao(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TipoConclusao fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        return Stream.of(TipoConclusao.values())
                .filter(t -> t.getValue().equalsIgnoreCase(value) || t.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de conclusão inválido: " + value));
    }
}