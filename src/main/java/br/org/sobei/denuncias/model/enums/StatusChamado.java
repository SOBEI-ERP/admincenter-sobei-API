package br.org.sobei.denuncias.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.stream.Stream;

@Getter
public enum StatusChamado {
    ABERTO("aberto"),
    EM_ANDAMENTO("em_andamento"),
    AGUARDANDO_INFORMACAO("aguardando_informacao"),
    CONCLUIDO("concluido"),
    CANCELADO("cancelado");

    private final String value;

    StatusChamado(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StatusChamado fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        return Stream.of(StatusChamado.values())
                .filter(s -> s.getValue().equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status de chamado inválido: " + value));
    }
}
