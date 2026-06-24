package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.AtualizarVagaRequest;
import br.org.sobei.denuncias.dto.request.CriarVagaRequest;
import br.org.sobei.denuncias.dto.response.VagaResponse;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.entity.Vaga;
import br.org.sobei.denuncias.model.enums.ModalidadeVaga;
import br.org.sobei.denuncias.model.enums.NivelAdmin;
import br.org.sobei.denuncias.model.enums.StatusVaga;
import br.org.sobei.denuncias.model.enums.TipoContrato;
import br.org.sobei.denuncias.repository.CandidaturaRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import br.org.sobei.denuncias.repository.VagaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VagaServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VagaService vagaService;

    @Test
    void testCriarVagaComSucesso() {
        Usuario admin = Usuario.builder()
                .id(1)
                .email("diretora@sobei.org.br")
                .nivel(NivelAdmin.diretora)
                .unidade("Imbuias")
                .build();

        CriarVagaRequest request = new CriarVagaRequest();
        request.setTitulo("Auxiliar Administrativo");
        request.setDepartamento("Administrativo");
        request.setDescricao("Descrição da vaga");
        request.setRequisitos("Requisitos");
        request.setModalidade(ModalidadeVaga.PRESENCIAL);
        request.setTipoContrato(TipoContrato.CLT);

        when(usuarioRepository.findByEmail("diretora@sobei.org.br")).thenReturn(Optional.of(admin));
        when(vagaRepository.save(any(Vaga.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VagaResponse response = vagaService.criar(request, "diretora@sobei.org.br");

        assertNotNull(response);
        assertEquals("Auxiliar Administrativo", response.getTitulo());
        assertEquals("Imbuias", response.getUnidade());
        assertEquals(StatusVaga.ABERTA, response.getStatus());

        verify(vagaRepository, times(1)).save(any(Vaga.class));
    }

    @Test
    void testCriarVagaSemPermissaoDiretoraThrowsException() {
        Usuario admin = Usuario.builder()
                .id(1)
                .email("admin@sobei.org.br")
                .nivel(NivelAdmin.admin) // Não é diretora
                .unidade("Imbuias")
                .build();

        CriarVagaRequest request = new CriarVagaRequest();

        when(usuarioRepository.findByEmail("admin@sobei.org.br")).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> {
            vagaService.criar(request, "admin@sobei.org.br");
        });

        verify(vagaRepository, never()).save(any(Vaga.class));
    }

    @Test
    void testCriarVagaDiretoraSemUnidadeThrowsException() {
        Usuario admin = Usuario.builder()
                .id(1)
                .email("diretora@sobei.org.br")
                .nivel(NivelAdmin.diretora)
                .unidade(null) // Sem unidade
                .build();

        CriarVagaRequest request = new CriarVagaRequest();

        when(usuarioRepository.findByEmail("diretora@sobei.org.br")).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> {
            vagaService.criar(request, "diretora@sobei.org.br");
        });

        verify(vagaRepository, never()).save(any(Vaga.class));
    }

    @Test
    void testListarPorUnidadeSucesso() {
        Usuario admin = Usuario.builder()
                .id(1)
                .email("diretora@sobei.org.br")
                .nivel(NivelAdmin.diretora)
                .unidade("Imbuias")
                .build();

        Vaga vaga1 = Vaga.builder().id(1).titulo("Vaga 1").unidade("Imbuias").status(StatusVaga.ABERTA).build();
        Vaga vaga2 = Vaga.builder().id(2).titulo("Vaga 2").unidade("Imbuias").status(StatusVaga.PAUSADA).build();

        when(usuarioRepository.findByEmail("diretora@sobei.org.br")).thenReturn(Optional.of(admin));
        when(vagaRepository.findByUnidadeOrderByDataCriacaoDesc("Imbuias")).thenReturn(List.of(vaga1, vaga2));

        List<VagaResponse> responseList = vagaService.listarPorUnidade("diretora@sobei.org.br");

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("Vaga 1", responseList.get(0).getTitulo());
    }

    @Test
    void testAtualizarVagaDiferenteUnidadeThrowsException() {
        Usuario admin = Usuario.builder()
                .id(1)
                .email("diretora@sobei.org.br")
                .nivel(NivelAdmin.diretora)
                .unidade("Imbuias")
                .build();

        Vaga vaga = Vaga.builder()
                .id(10)
                .titulo("Auxiliar")
                .unidade("Acácias") // Outra unidade
                .build();

        AtualizarVagaRequest request = new AtualizarVagaRequest();
        request.setTitulo("Auxiliar Administrativo II");

        when(usuarioRepository.findByEmail("diretora@sobei.org.br")).thenReturn(Optional.of(admin));
        when(vagaRepository.findById(10)).thenReturn(Optional.of(vaga));

        assertThrows(IllegalArgumentException.class, () -> {
            vagaService.atualizar(10, request, "diretora@sobei.org.br");
        });

        verify(vagaRepository, never()).save(any(Vaga.class));
    }
}
