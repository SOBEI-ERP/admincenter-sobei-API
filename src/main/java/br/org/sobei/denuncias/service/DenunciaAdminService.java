package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.AtualizarDenunciaRequest;
import br.org.sobei.denuncias.dto.request.MedidaAdotadaRequest;
import br.org.sobei.denuncias.dto.response.DenunciaAdminResponse;
import br.org.sobei.denuncias.dto.response.DenunciaDetalheResponse;
import br.org.sobei.denuncias.dto.response.MedidaAdotadaResponse;
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

    @Transactional(readOnly = true)
    public List<DenunciaAdminResponse> listarDenuncias(String status, String tipo, String unidade, String ordem) {
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

        Sort sort = "recentes".equalsIgnoreCase(ordem) ? Sort.by("dataAbertura").descending() : Sort.by("dataAbertura").ascending();

        List<Denuncia> denuncias = denunciaRepository.findAll(spec, sort);

        return denuncias.stream().map(d -> {
            java.time.LocalDateTime openedAt = d.getHistoricos().stream()
                    .filter(h -> h.getEstadoNovo() == br.org.sobei.denuncias.model.enums.StatusDenuncia.EM_ANDAMENTO)
                    .map(br.org.sobei.denuncias.model.entity.HistoricoEstado::getDataAlteracao)
                    .min(java.time.LocalDateTime::compareTo)
                    .orElse(null);

            java.time.LocalDateTime closedAt = (d.getConclusao() != null && d.getConclusao().getTipoConclusao() == br.org.sobei.denuncias.model.enums.TipoConclusao.FINAL)
                    ? d.getConclusao().getDataConclusao()
                    : null;

            java.time.LocalDateTime archivedAt = (d.getConclusao() != null && d.getConclusao().getTipoConclusao() == br.org.sobei.denuncias.model.enums.TipoConclusao.ARQUIVAMENTO)
                    ? d.getConclusao().getDataConclusao()
                    : null;

            return DenunciaAdminResponse.builder()
                    .id(d.getId())
                    .protocolo(d.getProtocolo())
                    .status(d.getEstado())
                    .tipo(d.getTipo())
                    .unidade(d.getUnidade())
                    .dataEnvio(d.getDataAbertura())
                    .dataAbertura(openedAt)
                    .ultimaAlteracao(d.getUltimaAlteracao())
                    .dataFechamento(closedAt)
                    .dataArquivamento(archivedAt)
                    .prioridade(d.getPrioridade())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DenunciaDetalheResponse buscarDetalhes(String protocolo) {
        Denuncia d = denunciaRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new IllegalArgumentException("Denúncia não encontrada."));

        List<MedidaAdotadaResponse> medidas = medidaAdotadaRepository.findByDenunciaIdOrderByDataRegistroAsc(d.getId())
                .stream().map(m -> MedidaAdotadaResponse.builder()
                        .id(m.getId())
                        .descricao(m.getDescricao())
                        .dataRegistro(m.getDataRegistro())
                        .build())
                .collect(Collectors.toList());

        ConclusaoDenuncia conclusao = conclusaoDenunciaRepository.findById(d.getId()).orElse(null);

        java.time.LocalDateTime openedAt = d.getHistoricos().stream()
                .filter(h -> h.getEstadoNovo() == br.org.sobei.denuncias.model.enums.StatusDenuncia.EM_ANDAMENTO)
                .map(br.org.sobei.denuncias.model.entity.HistoricoEstado::getDataAlteracao)
                .min(java.time.LocalDateTime::compareTo)
                .orElse(null);

        java.time.LocalDateTime closedAt = (conclusao != null && conclusao.getTipoConclusao() == br.org.sobei.denuncias.model.enums.TipoConclusao.FINAL)
                ? conclusao.getDataConclusao()
                : null;

        java.time.LocalDateTime archivedAt = (conclusao != null && conclusao.getTipoConclusao() == br.org.sobei.denuncias.model.enums.TipoConclusao.ARQUIVAMENTO)
                ? conclusao.getDataConclusao()
                : null;

        var builder = DenunciaDetalheResponse.builder()
                .id(d.getId())
                .protocolo(d.getProtocolo())
                .status(d.getEstado())
                .tipo(d.getTipo())
                .unidade(d.getUnidade())
                .dataEnvio(d.getDataAbertura())
                .dataAbertura(openedAt)
                .dataFechamento(closedAt)
                .dataArquivamento(archivedAt)
                .descricao(d.getDescricao())
                .envolvidos(d.getEnvolvidos())
                .testemunhas(d.getTestemunhas())
                .medidasAdotadas(medidas)
                .prioridade(d.getPrioridade());

        if (d.getDenunciante() != null) {
            builder.nomeDenunciante(d.getDenunciante().getNomeCompleto())
                    .emailDenunciante(d.getDenunciante().getEmail())
                    .telefoneDenunciante(d.getDenunciante().getTelefone());
        }

        if (conclusao != null) {
            builder.relatorioConclusao(conclusao.getRelatorio())
                    .tipoConclusao(conclusao.getTipoConclusao() != null ? conclusao.getTipoConclusao().name() : null);
        }

        return builder.build();
    }

    @Transactional
    public DenunciaDetalheResponse atualizarDenuncia(String protocolo, AtualizarDenunciaRequest request) {
        Denuncia d = denunciaRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new IllegalArgumentException("Denúncia não encontrada."));

        StatusDenuncia novoStatus = request.getStatus();

        // Validação de Fechamento/Arquivamento
        if ((novoStatus == StatusDenuncia.FECHADA || novoStatus == StatusDenuncia.ARQUIVADA) 
             && !StringUtils.hasText(request.getRelatorio())) {
            throw new IllegalArgumentException("Para fechar ou arquivar, o relatório de conclusão é obrigatório.");
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
                        medidaAdotadaRepository.save(medida);
                    }
                } else if (StringUtils.hasText(mReq.getDescricao())) {
                    MedidaAdotada medida = MedidaAdotada.builder()
                            .denuncia(d)
                            .descricao(mReq.getDescricao())
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
                    .build();
            medidaAdotadaRepository.save(medida);
        }

        // Se mudou o status
        if (d.getEstado() != novoStatus) {
            HistoricoEstado historico = HistoricoEstado.builder()
                    .denuncia(d)
                    .estadoAnterior(d.getEstado())
                    .estadoNovo(novoStatus)
                    .admin(null)
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
}
