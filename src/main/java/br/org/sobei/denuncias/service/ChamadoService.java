package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.AtualizarChamadoRequest;
import br.org.sobei.denuncias.dto.request.CriarChamadoRequest;
import br.org.sobei.denuncias.dto.response.ChamadoResponse;
import br.org.sobei.denuncias.model.entity.Chamado;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.enums.NivelAdmin;
import br.org.sobei.denuncias.model.enums.PrioridadeChamado;
import br.org.sobei.denuncias.model.enums.StatusChamado;
import br.org.sobei.denuncias.repository.ChamadoRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ChamadoResponse criar(CriarChamadoRequest request, String adminEmail) {
        Usuario admin = getSuporteAdmin(adminEmail);

        PrioridadeChamado prioridade = request.getPrioridade() != null ? request.getPrioridade() : PrioridadeChamado.MEDIA;

        Chamado chamado = Chamado.builder()
                .titulo(request.getTitulo().trim())
                .descricao(request.getDescricao().trim())
                .solicitante(request.getSolicitante().trim())
                .prioridade(prioridade)
                .status(StatusChamado.ABERTO)
                .prazoConclusao(request.getPrazoConclusao())
                .planoAcao(request.getPlanoAcao() != null ? request.getPlanoAcao().trim() : null)
                .usuario(admin)
                .build();

        Chamado salvo = chamadoRepository.save(chamado);
        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listar(String adminEmail, StatusChamado status, PrioridadeChamado prioridade) {
        getSuporteAdmin(adminEmail);

        List<Chamado> chamados;
        if (status != null && prioridade != null) {
            chamados = chamadoRepository.findByStatusAndPrioridadeOrderByDataCriacaoDesc(status, prioridade);
        } else if (status != null) {
            chamados = chamadoRepository.findByStatusOrderByDataCriacaoDesc(status);
        } else if (prioridade != null) {
            chamados = chamadoRepository.findByPrioridadeOrderByDataCriacaoDesc(prioridade);
        } else {
            chamados = chamadoRepository.findAllByOrderByDataCriacaoDesc();
        }

        return chamados.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscarPorId(Integer id, String adminEmail) {
        getSuporteAdmin(adminEmail);

        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado."));

        return toResponse(chamado);
    }

    @Transactional
    public ChamadoResponse atualizar(Integer id, AtualizarChamadoRequest request, String adminEmail) {
        getSuporteAdmin(adminEmail);

        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado."));

        if (request.getTitulo() != null && !request.getTitulo().isBlank()) {
            chamado.setTitulo(request.getTitulo().trim());
        }
        if (request.getDescricao() != null && !request.getDescricao().isBlank()) {
            chamado.setDescricao(request.getDescricao().trim());
        }
        if (request.getSolicitante() != null && !request.getSolicitante().isBlank()) {
            chamado.setSolicitante(request.getSolicitante().trim());
        }
        if (request.getPrioridade() != null) {
            chamado.setPrioridade(request.getPrioridade());
        }
        if (request.getPrazoConclusao() != null) {
            chamado.setPrazoConclusao(request.getPrazoConclusao());
        }
        if (request.getPlanoAcao() != null) {
            chamado.setPlanoAcao(request.getPlanoAcao().trim());
        }
        if (request.getResolucao() != null) {
            chamado.setResolucao(request.getResolucao().trim());
        }

        if (request.getStatus() != null) {
            StatusChamado novoStatus = request.getStatus();
            if ((novoStatus == StatusChamado.CONCLUIDO || novoStatus == StatusChamado.CANCELADO) &&
                    (chamado.getResolucao() == null || chamado.getResolucao().isBlank())) {
                throw new IllegalArgumentException("Ao concluir ou cancelar um chamado, é obrigatório registrar como o chamado foi encerrado.");
            }

            if ((novoStatus == StatusChamado.CONCLUIDO || novoStatus == StatusChamado.CANCELADO) && chamado.getDataEncerramento() == null) {
                chamado.setDataEncerramento(LocalDateTime.now());
            } else if (novoStatus != StatusChamado.CONCLUIDO && novoStatus != StatusChamado.CANCELADO) {
                chamado.setDataEncerramento(null);
            }

            chamado.setStatus(novoStatus);
        }

        Chamado salvo = chamadoRepository.save(chamado);
        return toResponse(salvo);
    }

    @Transactional
    public void deletar(Integer id, String adminEmail) {
        getSuporteAdmin(adminEmail);

        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado."));

        chamadoRepository.delete(chamado);
    }

    private Usuario getSuporteAdmin(String email) {
        Usuario admin = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (admin.getNivel() != NivelAdmin.suporte) {
            throw new IllegalArgumentException("Acesso restrito ao nível de suporte.");
        }

        return admin;
    }

    private ChamadoResponse toResponse(Chamado c) {
        return ChamadoResponse.builder()
                .id(c.getId())
                .titulo(c.getTitulo())
                .descricao(c.getDescricao())
                .solicitante(c.getSolicitante())
                .prioridade(c.getPrioridade())
                .status(c.getStatus())
                .prazoConclusao(c.getPrazoConclusao())
                .planoAcao(c.getPlanoAcao())
                .resolucao(c.getResolucao())
                .dataEncerramento(c.getDataEncerramento())
                .criadoPor(c.getUsuario() != null ? c.getUsuario().getUsuario() : "Sistema")
                .dataCriacao(c.getDataCriacao())
                .ultimaAlteracao(c.getUltimaAlteracao())
                .build();
    }
}
