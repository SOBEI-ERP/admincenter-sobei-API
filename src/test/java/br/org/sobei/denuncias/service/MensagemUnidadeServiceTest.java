package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.response.MensagemUnidadeResponse;
import br.org.sobei.denuncias.model.entity.MensagemUnidade;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.enums.NivelAdmin;
import br.org.sobei.denuncias.repository.MensagemUnidadeRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensagemUnidadeServiceTest {

    @Mock
    private MensagemUnidadeRepository mensagemUnidadeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MensagemUnidadeService mensagemUnidadeService;

    @Test
    @DisplayName("Coordenadora pedagógica lista mensagens da sua unidade com sucesso")
    void testCoordenadoraListaMensagensSuaUnidade() {
        Usuario coord = Usuario.builder()
                .email("coord@sobei.org.br")
                .nivel(NivelAdmin.coordenadora)
                .unidade("CEI Amigo")
                .build();

        MensagemUnidade msg = MensagemUnidade.builder()
                .id(1)
                .unidade("CEI Amigo")
                .nomeCompleto("João Silva")
                .email("joao@gmail.com")
                .telefone("11999999999")
                .mensagem("Dúvida sobre matrícula")
                .lida(false)
                .dataEnvio(LocalDateTime.now())
                .build();

        when(usuarioRepository.findByEmail("coord@sobei.org.br")).thenReturn(Optional.of(coord));
        when(mensagemUnidadeRepository.findByUnidadeContainingIgnoreCaseOrderByDataEnvioDesc("CEI Amigo"))
                .thenReturn(List.of(msg));

        List<MensagemUnidadeResponse> res = mensagemUnidadeService.listar("coord@sobei.org.br", null, false);

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("João Silva", res.get(0).getNomeCompleto());
        verify(mensagemUnidadeRepository).findByUnidadeContainingIgnoreCaseOrderByDataEnvioDesc("CEI Amigo");
    }

    @Test
    @DisplayName("Coordenadora de eventos sem unidade lista todas as mensagens com sucesso")
    void testCoordenadoraEventoListaTodasMensagens() {
        Usuario coordEvento = Usuario.builder()
                .email("coord.evento@sobei.org.br")
                .nivel(NivelAdmin.coordenadora_evento)
                .unidade(null)
                .build();

        MensagemUnidade msg1 = MensagemUnidade.builder()
                .id(1)
                .unidade("CEI Amigo")
                .nomeCompleto("Carlos")
                .email("carlos@gmail.com")
                .mensagem("Dúvida")
                .lida(false)
                .dataEnvio(LocalDateTime.now())
                .build();

        when(usuarioRepository.findByEmail("coord.evento@sobei.org.br")).thenReturn(Optional.of(coordEvento));
        when(mensagemUnidadeRepository.findAllByOrderByDataEnvioDesc()).thenReturn(List.of(msg1));

        List<MensagemUnidadeResponse> res = mensagemUnidadeService.listar("coord.evento@sobei.org.br", null, false);

        assertNotNull(res);
        assertEquals(1, res.size());
        verify(mensagemUnidadeRepository).findAllByOrderByDataEnvioDesc();
    }

    @Test
    @DisplayName("Coordenadora de eventos marca mensagem como lida com sucesso")
    void testCoordenadoraEventoMarcaComoLida() {
        Usuario coordEvento = Usuario.builder()
                .email("coord.evento@sobei.org.br")
                .nivel(NivelAdmin.coordenadora_evento)
                .unidade(null)
                .build();

        MensagemUnidade msg = MensagemUnidade.builder()
                .id(10)
                .unidade("CEI Esperança")
                .nomeCompleto("Ana")
                .email("ana@gmail.com")
                .lida(false)
                .dataEnvio(LocalDateTime.now())
                .build();

        when(usuarioRepository.findByEmail("coord.evento@sobei.org.br")).thenReturn(Optional.of(coordEvento));
        when(mensagemUnidadeRepository.findById(10)).thenReturn(Optional.of(msg));
        when(mensagemUnidadeRepository.save(any(MensagemUnidade.class))).thenAnswer(i -> i.getArgument(0));

        MensagemUnidadeResponse res = mensagemUnidadeService.marcarComoLida(10, "coord.evento@sobei.org.br");

        assertNotNull(res);
        assertTrue(res.getLida());
    }

    @Test
    @DisplayName("Coordenadora não pode alterar mensagem de outra unidade")
    void testCoordenadoraNaoPodeAlterarOutraUnidade() {
        Usuario coord = Usuario.builder()
                .email("coord@sobei.org.br")
                .nivel(NivelAdmin.coordenadora)
                .unidade("CEI Amigo")
                .build();

        MensagemUnidade msg = MensagemUnidade.builder()
                .id(5)
                .unidade("CEI Futuro")
                .nomeCompleto("Lucas")
                .lida(false)
                .build();

        when(usuarioRepository.findByEmail("coord@sobei.org.br")).thenReturn(Optional.of(coord));
        when(mensagemUnidadeRepository.findById(5)).thenReturn(Optional.of(msg));

        assertThrows(IllegalArgumentException.class, () -> {
            mensagemUnidadeService.marcarComoLida(5, "coord@sobei.org.br");
        });
    }
}
