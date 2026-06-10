package br.org.sobei.denuncias.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoConclusao {
    FINAL("final"),
    ARQUIVAMENTO("arquivamento");

    private final String value;

    public static TipoConclusao fromValue(String value) {
        for (TipoConclusao t : values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de conclusão desconhecido: " + value);
    }
}
