package br.org.sobei.denuncias.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Unidade {
    ACACIAS("Acácias"),
    ARAUCARIAS("Araucárias"),
    BELA_VISTA("Bela Vista"),
    CCINTER("CCINTER"),
    CEDESP("CEDESP"),
    CEDRO("Cedro"),
    CEREJEIRAS_JACOMO_TATTO("Cerejeiras/Jacomo Tatto"),
    IMBUIAS("Imbuias"),
    IPES("Ipês"),
    LEBLON("Leblon"),
    MACAUBAS("Macaúbas"),
    MATRIZ("Matriz"),
    MONTANARO("Montanaro"),
    NCI_IMBUIAS("NCI Imbuias"),
    OLIVEIRAS("Oliveiras"),
    ORQUIDEAS("Orquídeas"),
    SABIAS("Sabiás"),
    TELECENTRO("Telecentro");

    private final String descricao;

    public static Unidade fromDescricao(String descricao) {
        for (Unidade u : values()) {
            if (u.descricao.equalsIgnoreCase(descricao)) {
                return u;
            }
        }
        throw new IllegalArgumentException("Unidade desconhecida: " + descricao);
    }
}
