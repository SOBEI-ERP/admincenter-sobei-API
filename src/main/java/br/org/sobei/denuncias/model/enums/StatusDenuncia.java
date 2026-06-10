package br.org.sobei.denuncias.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusDenuncia {
    ABERTA("aberta"),
    EM_ANDAMENTO("em_andamento"),
    FECHADA("fechada"),
    ARQUIVADA("arquivada"),
    REABERTA("reaberta");

    private final String value;

    public static StatusDenuncia fromValue(String value) {
        for (StatusDenuncia s : values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status de denúncia desconhecido: " + value);
    }
}
