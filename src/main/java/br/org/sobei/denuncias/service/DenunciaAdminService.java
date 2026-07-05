package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.AtualizarDenunciaRequest;
import br.org.sobei.denuncias.dto.request.MedidaAdotadaRequest;
import br.org.sobei.denuncias.dto.response.DenunciaAdminResponse;
import br.org.sobei.denuncias.dto.response.DenunciaDetalheResponse;
import br.org.sobei.denuncias.dto.response.MedidaAdotadaResponse;
import br.org.sobei.denuncias.dto.mapper.DenunciaMapper;
import br.org.sobei.denuncias.model.entity.ConclusaoDenuncia;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.entity.HistoricoEstado;
import br.org.sobei.denuncias.model.entity.MedidaAdotada;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import br.org.sobei.denuncias.repository.ConclusaoDenunciaRepository;
import br.org.sobei.denuncias.repository.DenunciaRepository;
import br.org.sobei.denuncias.repository.HistoricoEstadoRepository;
import br.org.sobei.denuncias.repository.MedidaAdotadaRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DenunciaAdminService {

    private final DenunciaRepository denunciaRepository;
    private final MedidaAdotadaRepository medidaAdotadaRepository;
    private final HistoricoEstadoRepository historicoEstadoRepository;
    private final ConclusaoDenunciaRepository conclusaoDenunciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<DenunciaAdminResponse> listarDenuncias(String status, String tipo, String unidade, String ordem, String prioridadeOrdem, String protocolo, String dataInicio, String dataFim) {
        Specification<Denuncia> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), StatusDenuncia.valueOf(status.toUpperCase())));
        }
        if (StringUtils.hasText(tipo)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), TipoDenuncia.valueOf(tipo.toUpperCase())));
        }
        if (StringUtils.hasText(unidade)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("unidade"), unidade));
        }
        if (StringUtils.hasText(protocolo)) {
            spec = spec.and((root, query, cb) -> cb.like(cb.upper(root.get("protocolo")), "%" + protocolo.trim().toUpperCase() + "%"));
        }
        if (StringUtils.hasText(dataInicio)) {
            try {
                java.time.LocalDate startDate = java.time.LocalDate.parse(dataInicio);
                java.time.LocalDateTime startDateTime = startDate.atStartOfDay();
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataAbertura"), startDateTime));
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        if (StringUtils.hasText(dataFim)) {
            try {
                java.time.LocalDate endDate = java.time.LocalDate.parse(dataFim);
                java.time.LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999);
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataAbertura"), endDateTime));
            } catch (Exception e) {
                // Ignore parse errors
            }
        }

        Sort sort = "antigos".equalsIgnoreCase(ordem) ? Sort.by("dataAbertura").ascending() : Sort.by("dataAbertura").descending();

        List<Denuncia> denuncias = denunciaRepository.findAll(spec, sort);

        if ("maior_prioridade".equalsIgnoreCase(prioridadeOrdem)) {
            denuncias.sort((d1, d2) -> {
                int w1 = getPrioridadeWeight(d1.getPrioridade());
                int w2 = getPrioridadeWeight(d2.getPrioridade());
                if (w1 != w2) {
                    return Integer.compare(w2, w1);
                }
                if ("antigos".equalsIgnoreCase(ordem)) {
                    return d1.getDataAbertura().compareTo(d2.getDataAbertura());
                } else {
                    return d2.getDataAbertura().compareTo(d1.getDataAbertura());
                }
            });
        } else if ("menor_prioridade".equalsIgnoreCase(prioridadeOrdem)) {
            denuncias.sort((d1, d2) -> {
                int w1 = getPrioridadeWeight(d1.getPrioridade());
                int w2 = getPrioridadeWeight(d2.getPrioridade());
                if (w1 != w2) {
                    return Integer.compare(w1, w2);
                }
                if ("antigos".equalsIgnoreCase(ordem)) {
                    return d1.getDataAbertura().compareTo(d2.getDataAbertura());
                } else {
                    return d2.getDataAbertura().compareTo(d1.getDataAbertura());
                }
            });
        }

        return denuncias.stream()
                .map(DenunciaMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DenunciaDetalheResponse buscarDetalhes(String protocolo) {
        Denuncia d = denunciaRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new IllegalArgumentException("Denúncia não encontrada."));

        List<MedidaAdotadaResponse> medidas = DenunciaMapper.toMedidaResponseList(
                medidaAdotadaRepository.findByDenunciaIdOrderByDataRegistroAsc(d.getId())
        );

        ConclusaoDenuncia conclusao = conclusaoDenunciaRepository.findById(d.getId()).orElse(null);

        return DenunciaMapper.toDetalheResponse(d, medidas, conclusao);
    }

    @Transactional
    public DenunciaDetalheResponse atualizarDenuncia(String protocolo, AtualizarDenunciaRequest request, String adminEmail) {
        Denuncia d = denunciaRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new IllegalArgumentException("Denúncia não encontrada."));

        StatusDenuncia novoStatus = request.getStatus();

        // Validação de Fechamento/Arquivamento
        if ((novoStatus == StatusDenuncia.FECHADA || novoStatus == StatusDenuncia.ARQUIVADA) 
             && !StringUtils.hasText(request.getRelatorio())) {
            throw new IllegalArgumentException("Para fechar ou arquivar, o relatório de conclusão é obrigatório.");
        }

        br.org.sobei.denuncias.model.entity.Usuario usuarioLogado = null;
        if (adminEmail != null) {
            usuarioLogado = usuarioRepository.findByEmail(adminEmail).orElse(null);
        }

        // Se enviou lista de medidas para atualizar/inserir/excluir
        if (request.getMedidas() != null) {
            for (MedidaAdotadaRequest mReq : request.getMedidas()) {
                if (mReq.getId() != null) {
                    MedidaAdotada medida = medidaAdotadaRepository.findById(mReq.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Medida não encontrada: " + mReq.getId()));
                    if (!medida.getDenuncia().getId().equals(d.getId())) {
                        throw new IllegalArgumentException("A medida não pertence a esta denúncia.");
                    }
                    if (!StringUtils.hasText(mReq.getDescricao())) {
                        medidaAdotadaRepository.delete(medida);
                    } else {
                        medida.setDescricao(mReq.getDescricao());
                        medida.setAdmin(usuarioLogado);
                        medidaAdotadaRepository.save(medida);
                    }
                } else if (StringUtils.hasText(mReq.getDescricao())) {
                    MedidaAdotada medida = MedidaAdotada.builder()
                            .denuncia(d)
                            .descricao(mReq.getDescricao())
                            .admin(usuarioLogado)
                            .build();
                    medidaAdotadaRepository.save(medida);
                }
            }
        }

        // Se adicionou medida via campo antigo descricaoAcao (compatibilidade)
        if (StringUtils.hasText(request.getDescricaoAcao())) {
            MedidaAdotada medida = MedidaAdotada.builder()
                    .denuncia(d)
                    .descricao(request.getDescricaoAcao())
                    .admin(usuarioLogado)
                    .build();
            medidaAdotadaRepository.save(medida);
        }

        // Se mudou o status
        if (d.getEstado() != novoStatus) {
            HistoricoEstado historico = HistoricoEstado.builder()
                    .denuncia(d)
                    .estadoAnterior(d.getEstado())
                    .estadoNovo(novoStatus)
                    .admin(usuarioLogado)
                    .build();
            historicoEstadoRepository.save(historico);
            
            d.setEstado(novoStatus);
        }

        // Se tem relatório e está finalizando
        if (StringUtils.hasText(request.getRelatorio()) && 
           (novoStatus == StatusDenuncia.FECHADA || novoStatus == StatusDenuncia.ARQUIVADA)) {
            ConclusaoDenuncia conclusao = ConclusaoDenuncia.builder()
                    .denuncia(d)
                    .relatorio(request.getRelatorio())
                    .tipoConclusao(request.getTipoConclusao())
                    .admin(usuarioLogado)
                    .build();
            conclusaoDenunciaRepository.save(conclusao);
        }


        if (request.getPrioridade() != null) {
            if (d.getEstado() != StatusDenuncia.EM_ANDAMENTO && novoStatus != StatusDenuncia.EM_ANDAMENTO) {
                throw new IllegalArgumentException(
                        "A prioridade só pode ser alterada quando a denúncia está em andamento.");
            }
            d.setPrioridade(request.getPrioridade());
        }

        denunciaRepository.save(d);
        return buscarDetalhes(d.getProtocolo());
    }

    private int getPrioridadeWeight(br.org.sobei.denuncias.model.enums.PrioridadeDenuncia prioridade) {
        if (prioridade == null) return 0;
        return switch (prioridade) {
            case ALTA -> 4;
            case MEDIA -> 3;
            case BAIXA -> 2;
            case NEUTRA -> 1;
        };
    }
}
