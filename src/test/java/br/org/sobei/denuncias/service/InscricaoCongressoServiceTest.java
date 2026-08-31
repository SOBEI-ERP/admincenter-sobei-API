package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.CriarInscricaoCongressoRequest;
import br.org.sobei.denuncias.dto.response.InscricaoCongressoResponse;
import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import br.org.sobei.denuncias.repository.InscricaoCongressoRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscricaoCongressoServiceTest {

    @Mock
    private InscricaoCongressoRepository inscricaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CertificadoCongressoService certificadoService;

    @Mock
    private CrachaCongressoService crachaService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InscricaoCongressoService inscricaoService;

    private CriarInscricaoCongressoRequest request;

    @BeforeEach
    void setUp() {
        request = new CriarInscricaoCongressoRequest();
        request.setNomeCompleto("João da Silva");
        // CPF válido para teste de validação algorítmica
        request.setCpf("486.932.818-60");
        request.setEmail("joao.silva@exemplo.com");
        request.setTipoOsc("SOBEI");
        request.setUnidade("CCINTER");
    }

    @Test
    @DisplayName("Deve criar inscrição com sucesso quando CPF e E-mail são únicos")
    void deveCriarInscricaoComSucesso() {
        when(inscricaoRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(inscricaoRepository.existsByEmailIgnoreCase("joao.silva@exemplo.com")).thenReturn(false);
        when(inscricaoRepository.save(any(InscricaoCongresso.class))).thenAnswer(invocation -> {
            InscricaoCongresso entity = invocation.getArgument(0);
            entity.setId(1);
            return entity;
        });

        InscricaoCongressoResponse response = inscricaoService.criar(request);

        assertNotNull(response);
        assertEquals("João da Silva", response.getNomeCompleto());
        assertEquals("joao.silva@exemplo.com", response.getEmail());
        verify(inscricaoRepository, times(1)).save(any(InscricaoCongresso.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o e-mail já está cadastrado em outra inscrição")
    void deveLancarExcecaoQuandoEmailJaExiste() {
        when(inscricaoRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(inscricaoRepository.existsByEmailIgnoreCase("joao.silva@exemplo.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inscricaoService.criar(request);
        });

        assertEquals("Este e-mail já está cadastrado em outra inscrição do Congresso.", exception.getMessage());
        verify(inscricaoRepository, never()).save(any(InscricaoCongresso.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o CPF já está cadastrado")
    void deveLancarExcecaoQuandoCpfJaExiste() {
        when(inscricaoRepository.findByCpf("486.932.818-60")).thenReturn(Optional.of(new InscricaoCongresso()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inscricaoService.criar(request);
        });

        assertEquals("Este CPF já está inscrito no Congresso.", exception.getMessage());
        verify(inscricaoRepository, never()).save(any(InscricaoCongresso.class));
    }
}
