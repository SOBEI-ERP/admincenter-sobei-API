package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.CriarDenunciaRequest;
import br.org.sobei.denuncias.dto.response.ConsultaProtocoloResponse;
import br.org.sobei.denuncias.dto.response.CriarDenunciaResponse;
import br.org.sobei.denuncias.model.entity.ConclusaoDenuncia;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.entity.MedidaAdotada;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoConclusao;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import br.org.sobei.denuncias.model.enums.Unidade;
import br.org.sobei.denuncias.repository.ConclusaoDenunciaRepository;
import br.org.sobei.denuncias.repository.DenunciaRepository;
import br.org.sobei.denuncias.repository.MedidaAdotadaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DenunciaServiceTest {

    @Mock
    private DenunciaRepository denunciaRepository;

    @Mock
    private ProtocoloService protocoloService;

    @Mock
    private MedidaAdotadaRepository medidaAdotadaRepository;

    @Mock
    private ConclusaoDenunciaRepository conclusaoDenunciaRepository;

    @InjectMocks
    private DenunciaService denunciaService;

    @Test
    void testCriarDenunciaAnonimaSucesso() {
        // Arrange
        String protocoloEsperado = "ABC-123-456";
        when(protocoloService.gerarProtocolo()).thenReturn(protocoloEsperado);

        CriarDenunciaRequest request = new CriarDenunciaRequest();
        request.setTipo(TipoDenuncia.ANONIMA);
        request.setUnidade("CCINTER");
        request.setDescricao("Descrição de teste anônimo");
        request.setEnvolvidos("Envolvido A");
        request.setTestemunhas("Testemunha B");

        // Act
        CriarDenunciaResponse response = denunciaService.criarDenuncia(request);

        // Assert
        assertNotNull(response);
        assertEquals(protocoloEsperado, response.getProtocolo());
        assertEquals("Denúncia registrada com sucesso.", response.getMensagem());

        verify(protocoloService, times(1)).gerarProtocolo();
        verify(denunciaRepository, times(1)).save(argThat(denuncia -> {
            assertEquals(protocoloEsperado, denuncia.getProtocolo());
            assertEquals(TipoDenuncia.ANONIMA, denuncia.getTipo());
            assertEquals("CCINTER", denuncia.getUnidade());
            assertEquals("Descrição de teste anônimo", denuncia.getDescricao());
            assertEquals("Envolvido A", denuncia.getEnvolvidos());
            assertEquals("Testemunha B", denuncia.getTestemunhas());
            assertEquals(StatusDenuncia.NA_FILA, denuncia.getEstado());
            assertNull(denuncia.getDenunciante());
            return true;
        }));
    }

    @Test
    void testCriarDenunciaIdentificadaSucesso() {
        // Arrange
        String protocoloEsperado = "XYZ-987-654";
        when(protocoloService.gerarProtocolo()).thenReturn(protocoloEsperado);

        CriarDenunciaRequest request = new CriarDenunciaRequest();
        request.setTipo(TipoDenuncia.IDENTIFICADA);
        request.setUnidade("Araucárias");
        request.setDescricao("Descrição de teste identificado");
        request.setNomeCompleto("João da Silva");
        request.setEmail("joao@exemplo.com");
        request.setTelefone("(11) 99999-8888");

        // Act
        CriarDenunciaResponse response = denunciaService.criarDenuncia(request);

        // Assert
        assertNotNull(response);
        assertEquals(protocoloEsperado, response.getProtocolo());

        verify(denunciaRepository, times(1)).save(argThat(denuncia -> {
            assertEquals(protocoloEsperado, denuncia.getProtocolo());
            assertEquals(TipoDenuncia.IDENTIFICADA, denuncia.getTipo());
            assertNotNull(denuncia.getDenunciante());
            assertEquals("João da Silva", denuncia.getDenunciante().getNomeCompleto());
            assertEquals("joao@exemplo.com", denuncia.getDenunciante().getEmail());
            assertEquals("(11) 99999-8888", denuncia.getDenunciante().getTelefone());
            return true;
        }));
    }

    @Test
    void testConsultarPorProtocoloSucessoSemConclusao() {
        // Arrange
        String protocolo = "ABC-123-456";
        Denuncia denuncia = Denuncia.builder()
                .id(10)
                .protocolo(protocolo)
                .tipo(TipoDenuncia.ANONIMA)
                .unidade("CCINTER")
                .descricao("Minha denúncia")
                .estado(StatusDenuncia.EM_ANDAMENTO)
                .dataAbertura(LocalDateTime.now())
                .ultimaAlteracao(LocalDateTime.now())
                .build();

        when(denunciaRepository.findByProtocolo(protocolo)).thenReturn(Optional.of(denuncia));
        when(conclusaoDenunciaRepository.findById(10)).thenReturn(Optional.empty());

        // Act
        ConsultaProtocoloResponse response = denunciaService.consultarPorProtocolo(protocolo);

        // Assert
        assertNotNull(response);
        assertEquals(protocolo, response.getProtocolo());
        assertEquals(StatusDenuncia.EM_ANDAMENTO, response.getEstado());
        assertNull(response.getRelatorioConclusao());
        assertNull(response.getTipoConclusao());
    }

    @Test
    void testConsultarPorProtocoloSucessoComConclusao() {
        // Arrange
        String protocolo = "XYZ-789-012";
        Denuncia denuncia = Denuncia.builder()
                .id(20)
                .protocolo(protocolo)
                .tipo(TipoDenuncia.IDENTIFICADA)
                .unidade("Araucárias")
                .descricao("Identificado")
                .estado(StatusDenuncia.FECHADA)
                .dataAbertura(LocalDateTime.now())
                .ultimaAlteracao(LocalDateTime.now())
                .build();

        when(denunciaRepository.findByProtocolo(protocolo)).thenReturn(Optional.of(denuncia));

        ConclusaoDenuncia conclusao = ConclusaoDenuncia.builder()
                .id(20)
                .tipoConclusao(TipoConclusao.FINAL)
                .relatorio("Investigação concluída e providências tomadas.")
                .build();
        when(conclusaoDenunciaRepository.findById(20)).thenReturn(Optional.of(conclusao));

        // Act
        ConsultaProtocoloResponse response = denunciaService.consultarPorProtocolo(protocolo);

        // Assert
        assertNotNull(response);
        assertEquals(protocolo, response.getProtocolo());
        assertEquals(StatusDenuncia.FECHADA, response.getEstado());
        assertEquals("Investigação concluída e providências tomadas.", response.getRelatorioConclusao());
        assertEquals("FINAL", response.getTipoConclusao());
    }

    @Test
    void testConsultarPorProtocoloNaoEncontrado() {
        // Arrange
        String protocoloInexistente = "INVALID-123";
        when(denunciaRepository.findByProtocolo(protocoloInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            denunciaService.consultarPorProtocolo(protocoloInexistente);
        });

        assertEquals("Protocolo não encontrado.", exception.getMessage());
    }
}
