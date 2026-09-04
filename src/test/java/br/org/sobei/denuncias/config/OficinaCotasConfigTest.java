package br.org.sobei.denuncias.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OficinaCotasConfigTest {

    @Test
    @DisplayName("Deve retornar cotas corretas por unidade para a oficina do Rodrigo Cândido")
    void deveRetornarCotasRodrigoCandido() {
        assertEquals(8, OficinaCotasConfig.obterCotaUnidade("Rodrigo Cândido", "Montanaro"));
        assertEquals(8, OficinaCotasConfig.obterCotaUnidade("Quem dança seus males espanta!", "CEI Montanaro"));
        assertEquals(3, OficinaCotasConfig.obterCotaUnidade("Rodrigo Cândido", "CEI Leblon"));
        assertEquals(4, OficinaCotasConfig.obterCotaUnidade("Rodrigo Cândido", "CEI Imbuias"));
        assertEquals(5, OficinaCotasConfig.obterCotaUnidade("Rodrigo Cândido", "CEI Bela Vista"));
        assertEquals(8, OficinaCotasConfig.obterCotaUnidade("Rodrigo Cândido", "CEI Orquídeas"));
    }

    @Test
    @DisplayName("Deve retornar cotas corretas para a oficina da Regiane Lays")
    void deveRetornarCotasRegianeLays() {
        assertEquals(7, OficinaCotasConfig.obterCotaUnidade("Regiane Lays Jacinto de Brito", "Montanaro"));
        assertEquals(9, OficinaCotasConfig.obterCotaUnidade("Saberes que alimentam", "CEI Orquídeas"));
        assertEquals(4, OficinaCotasConfig.obterCotaUnidade("Regiane Lays", "CEI Sabiás"));
    }

    @Test
    @DisplayName("Deve normalizar variações de nomes de unidade")
    void deveNormalizarUnidades() {
        assertEquals("MONTANARO", OficinaCotasConfig.normalizarUnidade("CEI Montanaro"));
        assertEquals("LEBLON", OficinaCotasConfig.normalizarUnidade("CEI Leblon"));
        assertEquals("CEREJEIRAS", OficinaCotasConfig.normalizarUnidade("CEI Cerejeiras / Jacomo Tatto"));
        assertEquals("SABIAS", OficinaCotasConfig.normalizarUnidade("CEI Sabiás"));
        assertEquals("ORQUIDEAS", OficinaCotasConfig.normalizarUnidade("Orquídeas"));
        assertEquals("ARAUCARIAS", OficinaCotasConfig.normalizarUnidade("CEI Araucárias"));
    }

    @Test
    @DisplayName("Deve retornar cotas corretas para a oficina da Shirley da Silva")
    void deveRetornarCotasShirleySilva() {
        assertEquals(2, OficinaCotasConfig.obterCotaUnidade("Shirley da Silva", "Montanaro"));
        assertEquals(2, OficinaCotasConfig.obterCotaUnidade("Motricidade Livre", "CEI Montanaro"));
        assertEquals(1, OficinaCotasConfig.obterCotaUnidade("Shirley da Silva", "CEI Leblon"));
        assertEquals(3, OficinaCotasConfig.obterCotaUnidade("Motricidade Livre", "CEI Orquídeas"));
    }
}
