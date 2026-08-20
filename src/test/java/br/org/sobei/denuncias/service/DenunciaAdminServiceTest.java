package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.AtualizarDenunciaRequest;
import br.org.sobei.denuncias.dto.response.DenunciaDetalheResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoConclusao;
import br.org.sobei.denuncias.repository.ConclusaoDenunciaRepository;
import br.org.sobei.denuncias.repository.DenunciaRepository;
import br.org.sobei.denuncias.repository.HistoricoEstadoRepository;
import br.org.sobei.denuncias.repository.MedidaAdotadaRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DenunciaAdminServiceTest {

    @Mock
    private DenunciaRepository denunciaRepository;
    @Mock
    private MedidaAdotadaRepository medidaAdotadaRepository;
    @Mock
    private HistoricoEstadoRepository historicoEstadoRepository;
    @Mock
    private ConclusaoDenunciaRepository conclusaoDenunciaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DenunciaAdminService denunciaAdminService;

    @Test
    void testAtualizarDenunciaFecharSemRelatorioThrowsException() {
        Denuncia denuncia = Denuncia.builder().id(1).protocolo("XYZ-123-456").estado(StatusDenuncia.NA_FILA).build();
        when(denunciaRepository.findByProtocolo("XYZ-123-456")).thenReturn(Optional.of(denuncia));

        AtualizarDenunciaRequest request = new AtualizarDenunciaRequest();
        request.setStatus(StatusDenuncia.FECHADA);
        request.setRelatorio(null);

        assertThrows(IllegalArgumentException.class, () -> {
            denunciaAdminService.atualizarDenuncia("XYZ-123-456", request, "admin@sobei.org.br");
        });
    }

    @Test
    void testAtualizarDenunciaSucesso() {
        Denuncia denuncia = Denuncia.builder().id(1).protocolo("XYZ-123-456").estado(StatusDenuncia.NA_FILA).build();
        when(denunciaRepository.findByProtocolo("XYZ-123-456")).thenReturn(Optional.of(denuncia));

        AtualizarDenunciaRequest request = new AtualizarDenunciaRequest();
        request.setStatus(StatusDenuncia.FECHADA);
        request.setRelatorio("Relatorio concluido com sucesso.");
        request.setTipoConclusao(TipoConclusao.FINAL);

        DenunciaDetalheResponse response = denunciaAdminService.atualizarDenuncia("XYZ-123-456", request, "admin@sobei.org.br");

        assertNotNull(response);
        verify(conclusaoDenunciaRepository, times(1)).save(any());
        verify(historicoEstadoRepository, times(1)).save(any());
        verify(denunciaRepository, times(1)).save(any());
    }

    @Test
    void testAtualizarDenunciaComMedidaAdotadaEUsuarioLogado() {
        Denuncia denuncia = Denuncia.builder().id(1).protocolo("XYZ-123-456").estado(StatusDenuncia.EM_ANDAMENTO).build();
        when(denunciaRepository.findByProtocolo("XYZ-123-456")).thenReturn(Optional.of(denuncia));

        br.org.sobei.denuncias.model.entity.Usuario usuario = br.org.sobei.denuncias.model.entity.Usuario.builder()
                .id(2)
                .usuario("admin_teste")
                .email("admin_teste@sobei.org.br")
                .build();
        when(usuarioRepository.findByEmail("admin_teste@sobei.org.br")).thenReturn(Optional.of(usuario));

        AtualizarDenunciaRequest request = new AtualizarDenunciaRequest();
        request.setStatus(StatusDenuncia.EM_ANDAMENTO);
        
        br.org.sobei.denuncias.dto.request.MedidaAdotadaRequest mReq = new br.org.sobei.denuncias.dto.request.MedidaAdotadaRequest();
        mReq.setDescricao("Nova medida adotada teste");
        request.setMedidas(java.util.List.of(mReq));

        DenunciaDetalheResponse response = denunciaAdminService.atualizarDenuncia("XYZ-123-456", request, "admin_teste@sobei.org.br");

        assertNotNull(response);
        org.mockito.ArgumentCaptor<br.org.sobei.denuncias.model.entity.MedidaAdotada> captor = 
                org.mockito.ArgumentCaptor.forClass(br.org.sobei.denuncias.model.entity.MedidaAdotada.class);
        verify(medidaAdotadaRepository, times(1)).save(captor.capture());
        assertEquals("Nova medida adotada teste", captor.getValue().getDescricao());
        assertNotNull(captor.getValue().getAdmin());
        assertEquals("admin_teste", captor.getValue().getAdmin().getUsuario());
    }

    @Test
    void testDeletarDenunciaFechadaSuporteSucesso() {
        Denuncia denuncia = Denuncia.builder().id(1).protocolo("XYZ-123-456").estado(StatusDenuncia.FECHADA).build();
        br.org.sobei.denuncias.model.entity.Usuario suporteUser = br.org.sobei.denuncias.model.entity.Usuario.builder()
                .id(10)
                .email("suporte@sobei.org.br")
                .nivel(br.org.sobei.denuncias.model.enums.NivelAdmin.suporte)
                .build();

        when(usuarioRepository.findByEmail("suporte@sobei.org.br")).thenReturn(Optional.of(suporteUser));
        when(denunciaRepository.findByProtocolo("XYZ-123-456")).thenReturn(Optional.of(denuncia));

        denunciaAdminService.deletarDenunciaFechada("XYZ-123-456", "suporte@sobei.org.br");

        verify(denunciaRepository, times(1)).delete(denuncia);
    }

    @Test
    void testDeletarDenunciaFechadaNaoSuporteThrowsException() {
        br.org.sobei.denuncias.model.entity.Usuario adminUser = br.org.sobei.denuncias.model.entity.Usuario.builder()
                .id(11)
                .email("dp@sobei.org.br")
                .nivel(br.org.sobei.denuncias.model.enums.NivelAdmin.dp)
                .build();

        when(usuarioRepository.findByEmail("dp@sobei.org.br")).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class, () -> {
            denunciaAdminService.deletarDenunciaFechada("XYZ-123-456", "admin@sobei.org.br");
        });
    }

    @Test
    void testDeletarDenunciaNaoFechadaThrowsException() {
        Denuncia denuncia = Denuncia.builder().id(1).protocolo("XYZ-123-456").estado(StatusDenuncia.EM_ANDAMENTO).build();
        br.org.sobei.denuncias.model.entity.Usuario suporteUser = br.org.sobei.denuncias.model.entity.Usuario.builder()
                .id(10)
                .email("suporte@sobei.org.br")
                .nivel(br.org.sobei.denuncias.model.enums.NivelAdmin.suporte)
                .build();

        when(usuarioRepository.findByEmail("suporte@sobei.org.br")).thenReturn(Optional.of(suporteUser));
        when(denunciaRepository.findByProtocolo("XYZ-123-456")).thenReturn(Optional.of(denuncia));

        assertThrows(IllegalArgumentException.class, () -> {
            denunciaAdminService.deletarDenunciaFechada("XYZ-123-456", "suporte@sobei.org.br");
        });
    }

}
