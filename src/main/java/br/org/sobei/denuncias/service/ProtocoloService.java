package br.org.sobei.denuncias.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class ProtocoloService {

    private static final String LETRAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMEROS = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String gerarProtocolo() {
        StringBuilder sb = new StringBuilder(11);
        
        // 3 letras aleatórias
        for (int i = 0; i < 3; i++) {
            sb.append(LETRAS.charAt(RANDOM.nextInt(LETRAS.length())));
        }
        sb.append('-');
        
        // 3 números aleatórios
        for (int i = 0; i < 3; i++) {
            sb.append(NUMEROS.charAt(RANDOM.nextInt(NUMEROS.length())));
        }
        sb.append('-');
        
        // 3 números aleatórios
        for (int i = 0; i < 3; i++) {
            sb.append(NUMEROS.charAt(RANDOM.nextInt(NUMEROS.length())));
        }
        
        return sb.toString();
    }
}
