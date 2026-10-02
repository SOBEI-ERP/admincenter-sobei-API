package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.response.BancoTalentoResponse;
import br.org.sobei.denuncias.dto.response.BancoTalentoVagaResponse;
import br.org.sobei.denuncias.model.entity.BancoTalento;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.entity.Vaga;
import br.org.sobei.denuncias.model.enums.NivelAdmin;
import br.org.sobei.denuncias.repository.BancoTalentoRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BancoTalentoServiceTest {

    @Mock
    private BancoTalentoRepository bancoTalentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private BancoTalentoService bancoTalentoService;

    @Test
    void testListarBancosDiretoraComSucesso() {
        Usuario admin = Usuario.builder()
                .id(1)
                .email("diretora@sobei.org.br")
                .nivel(NivelAdmin.diretora)
                .unidade("Imbuias")
                .build();

        Vaga vaga = Vaga.builder().id(10).titulo("Professora").unidade("Imbuias").build();
        BancoTalento t = BancoTalento.builder()
                .id(1)
                .vaga(vaga)
                .nomeCompleto("Candidata 1")
                .dataMovimentacao(LocalDateTime.now())
                .build();

        when(usuarioRepository.findByEmail("diretora@sobei.org.br")).thenReturn(Optional.of(admin));
        when(bancoTalentoRepository.findByVagaUnidadeOrderByDataMovimentacaoDesc("Imbuias")).thenReturn(List.of(t));

        List<BancoTalentoVagaResponse> response = bancoTalentoService.listarBancos("diretora@sobei.org.br", null);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Professora", response.get(0).getVagaTitulo());
        assertEquals(1, response.get(0).getTotalTalentos());
    }

    @Test
    void testListarTalentosPorVagaDiretoraDiferenteUnidadeThrowsException() {
        Usuario admin = Usuario.builder()
                .id(1)
                .email("diretora@sobei.org.br")
                .nivel(NivelAdmin.diretora)
                .unidade("Imbuias") // Unidade diferente
                .build();

        Vaga vaga = Vaga.builder().id(10).titulo("Professora").unidade("Acácias").build(); // Vaga de Acácias
        BancoTalento t = BancoTalento.builder()
                .id(1)
                .vaga(vaga)
                .nomeCompleto("Candidata 1")
                .build();

        when(usuarioRepository.findByEmail("diretora@sobei.org.br")).thenReturn(Optional.of(admin));
        when(bancoTalentoRepository.findByVagaIdOrderByDataEnvioOriginalDesc(10)).thenReturn(List.of(t));

        assertThrows(IllegalArgumentException.class, () -> {
            bancoTalentoService.listarTalentosPorVaga(10, "diretora@sobei.org.br");
        });
    }

    @Test
    void testListarTalentosPorVagaSuporteComSucesso() {
        Usuario admin = Usuario.builder()
                .id(2)
                .email("suporte@sobei.org.br")
                .nivel(NivelAdmin.suporte)
                .build();

        Vaga vaga = Vaga.builder().id(10).titulo("Professora").unidade("Acácias").build();
        BancoTalento t = BancoTalento.builder()
                .id(1)
                .vaga(vaga)
                .nomeCompleto("Candidata 1")
                .curriculoNome("curriculo.pdf")
                .dataEnvioOriginal(LocalDateTime.now())
                .dataMovimentacao(LocalDateTime.now())
                .build();

        when(usuarioRepository.findByEmail("suporte@sobei.org.br")).thenReturn(Optional.of(admin));
        when(bancoTalentoRepository.findByVagaIdOrderByDataEnvioOriginalDesc(10)).thenReturn(List.of(t));

        List<BancoTalentoResponse> response = bancoTalentoService.listarTalentosPorVaga(10, "suporte@sobei.org.br");

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Candidata 1", response.get(0).getNomeCompleto());
    }
}
