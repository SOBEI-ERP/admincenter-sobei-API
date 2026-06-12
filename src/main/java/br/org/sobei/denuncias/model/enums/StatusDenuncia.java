package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.stream.Stream;

@Getter
public enum StatusDenuncia {
    NA_FILA("na_fila"),
    EM_ANDAMENTO("em_andamento"),
    FECHADA("fechada"),
    ARQUIVADA("arquivada");

    private final String value;

    StatusDenuncia(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StatusDenuncia fromValue(String value) {
        return Stream.of(StatusDenuncia.values())
                .filter(s -> s.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status invalido: " + value));
    }
}