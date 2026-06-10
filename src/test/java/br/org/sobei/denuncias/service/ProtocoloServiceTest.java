package br.org.sobei.denuncias.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

class ProtocoloServiceTest {

    private final ProtocoloService protocoloService = new ProtocoloService();

    @Test
    void testGerarProtocoloFormatoCorreto() {
        String protocolo = protocoloService.gerarProtocolo();
        
        assertNotNull(protocolo);
        assertEquals(11, protocolo.length());
        
        // Verifica o formato ABC-123-456 usando expressão regular:
        // 3 letras maiúsculas, traço, 3 dígitos, traço, 3 dígitos
        assertTrue(protocolo.matches("^[A-Z]{3}-\\d{3}-\\d{3}$"), 
            "O protocolo deve estar no formato ABC-123-456. Valor gerado: " + protocolo);
    }

    @Test
    void testGerarProtocoloUnicidade() {
        Set<String> protocolos = new HashSet<>();
        int quantidadeParaTestar = 1000;
        
        for (int i = 0; i < quantidadeParaTestar; i++) {
            String protocolo = protocoloService.gerarProtocolo();
            protocolos.add(protocolo);
        }
        
        // Verifica se todos os protocolos gerados são únicos
        assertEquals(quantidadeParaTestar, CleanSize(protocolos.size()), 
            "Devem ser gerados protocolos únicos.");
    }

    private int CleanSize(int size) {
        return size;
    }

}
