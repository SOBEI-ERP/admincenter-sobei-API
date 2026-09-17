package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.CriarInscricaoCongressoRequest;
import br.org.sobei.denuncias.dto.response.InscricaoCongressoResponse;
import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.enums.NivelAdmin;
import br.org.sobei.denuncias.repository.InscricaoCongressoRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

    @Test
    @DisplayName("Deve atualizar oficina com sucesso")
    void deveAtualizarOficinaComSucesso() {
        Usuario admin = Usuario.builder()
                .id(1)
                .usuario("suporte")
                .email("suporte@sobei.org.br")
                .nivel(br.org.sobei.denuncias.model.enums.NivelAdmin.suporte)
                .build();

        InscricaoCongresso inscricao = InscricaoCongresso.builder()
                .id(10)
                .nomeCompleto("Maria Educadora")
                .cpf("123.456.789-00")
                .email("maria@sobei.org.br")
                .tipoOsc("SOBEI")
                .unidade("Montanaro")
                .build();

        when(usuarioRepository.findByEmail("suporte@sobei.org.br")).thenReturn(Optional.of(admin));
        when(inscricaoRepository.findById(10)).thenReturn(Optional.of(inscricao));
        when(inscricaoRepository.save(any(InscricaoCongresso.class))).thenAnswer(i -> i.getArgument(0));

        br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest req = br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest.builder()
                .oficina("Quem dança seus males espanta!")
                .build();

        InscricaoCongressoResponse res = inscricaoService.atualizarOficinas(10, req, "suporte@sobei.org.br");

        assertNotNull(res);
        assertEquals("Quem dança seus males espanta!", res.getOficina());
        assertEquals("Quem dança seus males espanta!", inscricao.getOficina());
        verify(inscricaoRepository, times(1)).save(inscricao);
    }

    @Test
    @DisplayName("Deve rejeitar atualização quando a cota da unidade na oficina já foi atingida para usuário não-suporte")
    void deveRejeitarAtualizacaoQuandoCotaUnidadeAtingida() {
        Usuario admin = Usuario.builder()
                .id(2)
                .usuario("coordenadora")
                .email("coordenadora@sobei.org.br")
                .nivel(br.org.sobei.denuncias.model.enums.NivelAdmin.coordenadora)
                .unidade("CEI Leblon")
                .build();

        InscricaoCongresso inscricaoAlvo = InscricaoCongresso.builder()
                .id(10)
                .nomeCompleto("Professora 2")
                .cpf("222.222.222-22")
                .email("prof2@sobei.org.br")
                .tipoOsc("SOBEI")
                .unidade("CEI Leblon")
                .build();

        InscricaoCongresso inscricaoJaAlocada = InscricaoCongresso.builder()
                .id(9)
                .nomeCompleto("Professora 1")
                .cpf("111.111.111-11")
                .email("prof1@sobei.org.br")
                .tipoOsc("SOBEI")
                .unidade("CEI Leblon")
                .oficina("Cleide Derenzi Valadas")
                .build();

        when(usuarioRepository.findByEmail("coordenadora@sobei.org.br")).thenReturn(Optional.of(admin));
        when(inscricaoRepository.findById(10)).thenReturn(Optional.of(inscricaoAlvo));
        when(inscricaoRepository.findAll()).thenReturn(List.of(inscricaoJaAlocada, inscricaoAlvo));

        br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest req = br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest.builder()
                .oficina("Cleide Derenzi Valadas")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                inscricaoService.atualizarOficinas(10, req, "coordenadora@sobei.org.br")
        );

        assertTrue(ex.getMessage().contains("A cota desta oficina para a unidade"));
        assertTrue(ex.getMessage().contains("já foi preenchida"));
        verify(inscricaoRepository, never()).save(inscricaoAlvo);
    }

    @Test
    @DisplayName("Deve permitir atualização de oficina mesmo quando a cota da unidade estiver atingida se usuário for SUPORTE")
    void devePermitirAtualizacaoMesmoComCotaAtingidaQuandoUsuarioForSuporte() {
        Usuario admin = Usuario.builder()
                .id(1)
                .usuario("suporte")
                .email("suporte@sobei.org.br")
                .nivel(br.org.sobei.denuncias.model.enums.NivelAdmin.suporte)
                .build();

        InscricaoCongresso inscricaoAlvo = InscricaoCongresso.builder()
                .id(10)
                .nomeCompleto("Professora 2")
                .cpf("222.222.222-22")
                .email("prof2@sobei.org.br")
                .tipoOsc("SOBEI")
                .unidade("CEI Leblon")
                .build();

        InscricaoCongresso inscricaoJaAlocada = InscricaoCongresso.builder()
                .id(9)
                .nomeCompleto("Professora 1")
                .cpf("111.111.111-11")
                .email("prof1@sobei.org.br")
                .tipoOsc("SOBEI")
                .unidade("CEI Leblon")
                .oficina("Cleide Derenzi Valadas")
                .build();

        when(usuarioRepository.findByEmail("suporte@sobei.org.br")).thenReturn(Optional.of(admin));
        when(inscricaoRepository.findById(10)).thenReturn(Optional.of(inscricaoAlvo));
        when(inscricaoRepository.save(any(InscricaoCongresso.class))).thenAnswer(i -> i.getArgument(0));

        br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest req = br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest.builder()
                .oficina("Cleide Derenzi Valadas")
                .build();

        InscricaoCongressoResponse res = inscricaoService.atualizarOficinas(10, req, "suporte@sobei.org.br");

        assertNotNull(res);
        assertEquals("Cleide Derenzi Valadas", res.getOficina());
        verify(inscricaoRepository, times(1)).save(inscricaoAlvo);
    }

    @Test
    @DisplayName("Deve rejeitar atualização quando cota de 10 vagas para outras OSCs já foi atingida")
    void deveRejeitarAtualizacaoQuandoCotaOutrasOscAtingida() {
        Usuario admin = Usuario.builder()
                .id(3)
                .usuario("diretora")
                .email("diretora@sobei.org.br")
                .nivel(NivelAdmin.diretora)
                .build();

        InscricaoCongresso inscricaoAlvo = InscricaoCongresso.builder()
                .id(99)
                .nomeCompleto("Participante Outra OSC")
                .email("outra@ong.org.br")
                .tipoOsc("OUTRA")
                .outraOsc("Instituto Cidadão")
                .build();

        java.util.List<InscricaoCongresso> existentes = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            existentes.add(InscricaoCongresso.builder()
                    .id(i)
                    .nomeCompleto("Participante " + i)
                    .tipoOsc("OUTRA")
                    .outraOsc("OSC " + i)
                    .oficina("Quem dança seus males espanta!")
                    .build());
        }
        existentes.add(inscricaoAlvo);

        when(usuarioRepository.findByEmail("diretora@sobei.org.br")).thenReturn(Optional.of(admin));
        when(inscricaoRepository.findById(99)).thenReturn(Optional.of(inscricaoAlvo));
        when(inscricaoRepository.findAll()).thenReturn(existentes);

        br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest req = br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest.builder()
                .oficina("Quem dança seus males espanta!")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                inscricaoService.atualizarOficinas(99, req, "diretora@sobei.org.br")
        );

        assertTrue(ex.getMessage().contains("outras OSCs"));
        assertTrue(ex.getMessage().contains("10/10"));
        verify(inscricaoRepository, never()).save(inscricaoAlvo);
    }

    @Test
    @DisplayName("Deve permitir atualização quando cota de 10 vagas para outras OSCs estiver atingida se usuário for SUPORTE")
    void devePermitirAtualizacaoQuandoCotaOutrasOscAtingidaSeUsuarioForSuporte() {
        Usuario suporte = Usuario.builder()
                .id(1)
                .usuario("suporte")
                .email("suporte@sobei.org.br")
                .nivel(NivelAdmin.suporte)
                .build();

        InscricaoCongresso inscricaoAlvo = InscricaoCongresso.builder()
                .id(99)
                .nomeCompleto("Participante Outra OSC")
                .email("outra@ong.org.br")
                .tipoOsc("OUTRA")
                .outraOsc("Instituto Cidadão")
                .build();

        java.util.List<InscricaoCongresso> existentes = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            existentes.add(InscricaoCongresso.builder()
                    .id(i)
                    .nomeCompleto("Participante " + i)
                    .tipoOsc("OUTRA")
                    .outraOsc("OSC " + i)
                    .oficina("Quem dança seus males espanta!")
                    .build());
        }
        existentes.add(inscricaoAlvo);

        when(usuarioRepository.findByEmail("suporte@sobei.org.br")).thenReturn(Optional.of(suporte));
        when(inscricaoRepository.findById(99)).thenReturn(Optional.of(inscricaoAlvo));
        when(inscricaoRepository.save(any(InscricaoCongresso.class))).thenAnswer(i -> i.getArgument(0));

        br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest req = br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest.builder()
                .oficina("Quem dança seus males espanta!")
                .build();

        InscricaoCongressoResponse res = inscricaoService.atualizarOficinas(99, req, "suporte@sobei.org.br");

        assertNotNull(res);
        assertEquals("Quem dança seus males espanta!", res.getOficina());
        verify(inscricaoRepository, times(1)).save(inscricaoAlvo);
    }

    @Test
    @DisplayName("Deve rejeitar criação de inscrição quando limite global de 900 for atingido")
    void deveRejeitarCriacaoQuandoLimite900Atingido() {
        when(inscricaoRepository.count()).thenReturn(900L);

        CriarInscricaoCongressoRequest req = CriarInscricaoCongressoRequest.builder()
                .nomeCompleto("Professor Novo")
                .cpf("123.456.789-00")
                .email("novo@sobei.org.br")
                .tipoOsc("SOBEI")
                .unidade("Montanaro")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                inscricaoService.criar(req)
        );

        assertTrue(ex.getMessage().contains("estão encerradas"));
        assertTrue(ex.getMessage().contains("900"));
        verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar status correto de vagas")
    void deveRetornarStatusCorretoDeVagas() {
        when(inscricaoRepository.count()).thenReturn(850L);

        java.util.Map<String, Object> status = inscricaoService.obterStatusVagas();

        assertEquals(850L, status.get("totalInscritos"));
        assertEquals(900L, status.get("limiteMaximo"));
        assertEquals(50L, status.get("vagasRestantes"));
        assertEquals(true, status.get("inscricoesAbertas"));
    }

    @Test
    @DisplayName("Deve excluir inscrição com sucesso quando usuário for SUPORTE")
    void deveExcluirInscricaoQuandoUsuarioForSuporte() {
        Usuario suporte = Usuario.builder()
                .id(1)
                .email("suporte@sobei.org.br")
                .nivel(NivelAdmin.suporte)
                .build();
        InscricaoCongresso inscricao = InscricaoCongresso.builder()
                .id(99)
                .nomeCompleto("Participante Exclusao")
                .cpf("111.222.333-44")
                .build();

        when(usuarioRepository.findByEmail("suporte@sobei.org.br")).thenReturn(Optional.of(suporte));
        when(inscricaoRepository.findById(99)).thenReturn(Optional.of(inscricao));

        assertDoesNotThrow(() -> inscricaoService.deletarInscricao(99, "suporte@sobei.org.br"));
        verify(inscricaoRepository, times(1)).delete(inscricao);
    }

    @Test
    @DisplayName("Deve rejeitar exclusão de inscrição quando usuário NÃO for SUPORTE")
    void deveRejeitarExclusaoQuandoUsuarioNaoForSuporte() {
        Usuario credenciador = Usuario.builder()
                .id(2)
                .email("credenciador@sobei.org.br")
                .nivel(NivelAdmin.credenciador)
                .build();

        when(usuarioRepository.findByEmail("credenciador@sobei.org.br")).thenReturn(Optional.of(credenciador));

        org.springframework.security.access.AccessDeniedException ex = assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> inscricaoService.deletarInscricao(99, "credenciador@sobei.org.br")
        );

        assertTrue(ex.getMessage().contains("Suporte"));
        verify(inscricaoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve bloquear geração e envio de certificado para credenciador quando não houver check-in em ambos os dias")
    void deveBloquearCertificadoParaCredenciadorSemCheckinAmbosDias() {
        Usuario credenciador = Usuario.builder()
                .id(2)
                .email("credenciador@sobei.org.br")
                .nivel(NivelAdmin.credenciador)
                .build();

        InscricaoCongresso inscricao = InscricaoCongresso.builder()
                .id(50)
                .nomeCompleto("Aluno Participante")
                .email("aluno@sobei.org.br")
                .presenteDia11(true)
                .presenteDia12(false)
                .build();

        when(usuarioRepository.findByEmail("credenciador@sobei.org.br")).thenReturn(Optional.of(credenciador));
        when(inscricaoRepository.findById(50)).thenReturn(Optional.of(inscricao));

        IllegalArgumentException exGerar = assertThrows(IllegalArgumentException.class,
                () -> inscricaoService.gerarCertificadoPdf(50, "credenciador@sobei.org.br")
        );
        assertTrue(exGerar.getMessage().contains("ambos os dias"));

        IllegalArgumentException exEnviar = assertThrows(IllegalArgumentException.class,
                () -> inscricaoService.enviarCertificado(50, "credenciador@sobei.org.br")
        );
        assertTrue(exEnviar.getMessage().contains("ambos os dias"));

        verify(certificadoService, never()).gerarCertificadoPdf(any());
        verify(emailService, never()).enviarCertificadoCongresso(any(), any());
    }

    @Test
    @DisplayName("Deve permitir geração e envio de certificado para credenciador quando houver check-in em ambos os dias")
    void devePermitirCertificadoParaCredenciadorComCheckinAmbosDias() {
        Usuario credenciador = Usuario.builder()
                .id(2)
                .email("credenciador@sobei.org.br")
                .nivel(NivelAdmin.credenciador)
                .build();

        InscricaoCongresso inscricao = InscricaoCongresso.builder()
                .id(50)
                .nomeCompleto("Aluno Presente Ambos Dias")
                .email("aluno@sobei.org.br")
                .presenteDia11(true)
                .presenteDia12(true)
                .build();

        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(usuarioRepository.findByEmail("credenciador@sobei.org.br")).thenReturn(Optional.of(credenciador));
        when(inscricaoRepository.findById(50)).thenReturn(Optional.of(inscricao));
        when(certificadoService.gerarCertificadoPdf(inscricao)).thenReturn(pdfBytes);
        when(emailService.enviarCertificadoCongresso(inscricao, pdfBytes)).thenReturn(true);

        byte[] gerado = inscricaoService.gerarCertificadoPdf(50, "credenciador@sobei.org.br");
        assertNotNull(gerado);

        boolean enviado = inscricaoService.enviarCertificado(50, "credenciador@sobei.org.br");
        assertTrue(enviado);

        verify(certificadoService, times(2)).gerarCertificadoPdf(inscricao);
        verify(emailService, times(1)).enviarCertificadoCongresso(inscricao, pdfBytes);
    }

    @Test
    @DisplayName("Deve permitir geração de certificado para perfil suporte mesmo sem check-in em ambos os dias")
    void devePermitirCertificadoParaSuporteMesmoSemCheckinAmbosDias() {
        Usuario suporte = Usuario.builder()
                .id(1)
                .email("suporte@sobei.org.br")
                .nivel(NivelAdmin.suporte)
                .build();

        InscricaoCongresso inscricao = InscricaoCongresso.builder()
                .id(50)
                .nomeCompleto("Participante Pendente")
                .email("pendente@sobei.org.br")
                .presenteDia11(false)
                .presenteDia12(false)
                .build();

        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(usuarioRepository.findByEmail("suporte@sobei.org.br")).thenReturn(Optional.of(suporte));
        when(inscricaoRepository.findById(50)).thenReturn(Optional.of(inscricao));
        when(certificadoService.gerarCertificadoPdf(inscricao)).thenReturn(pdfBytes);

        byte[] gerado = inscricaoService.gerarCertificadoPdf(50, "suporte@sobei.org.br");
        assertNotNull(gerado);
        verify(certificadoService, times(1)).gerarCertificadoPdf(inscricao);
    }

    @Test
    @DisplayName("Deve negar acesso a inscrições do congresso para perfil DP")
    void deveNegarAcessoCongressoParaPerfilDp() {
        Usuario dp = Usuario.builder()
                .id(10)
                .email("dp@sobei.org.br")
                .nivel(NivelAdmin.dp)
                .build();

        when(usuarioRepository.findByEmail("dp@sobei.org.br")).thenReturn(Optional.of(dp));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            inscricaoService.listar("dp@sobei.org.br", null, null, null, null);
        });

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            inscricaoService.alterarPresenca(1, 11, true, "dp@sobei.org.br");
        });
    }
}
