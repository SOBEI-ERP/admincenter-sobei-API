package br.org.sobei.denuncias.dto.mapper;

import br.org.sobei.denuncias.dto.response.MedidaAdotadaResponse;
import br.org.sobei.denuncias.dto.response.DenunciaAdminResponse;
import br.org.sobei.denuncias.dto.response.DenunciaDetalheResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.entity.MedidaAdotada;
import br.org.sobei.denuncias.model.entity.ConclusaoDenuncia;
import br.org.sobei.denuncias.model.entity.HistoricoEstado;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoConclusao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DenunciaMapper {

    public static LocalDateTime extractOpenedAt(Denuncia d) {
        if (d.getHistoricos() == null) return null;
        return d.getHistoricos().stream()
                .filter(h -> h.getEstadoNovo() == StatusDenuncia.EM_ANDAMENTO)
                .map(HistoricoEstado::getDataAlteracao)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    public static LocalDateTime extractClosedAt(Denuncia d) {
        ConclusaoDenuncia conclusao = d.getConclusao();
        if (conclusao != null && conclusao.getTipoConclusao() == TipoConclusao.FINAL) {
            return conclusao.getDataConclusao();
        }
        return null;
    }

    public static LocalDateTime extractArchivedAt(Denuncia d) {
        ConclusaoDenuncia conclusao = d.getConclusao();
        if (conclusao != null && conclusao.getTipoConclusao() == TipoConclusao.ARQUIVAMENTO) {
            return conclusao.getDataConclusao();
        }
        return null;
    }

    public static MedidaAdotadaResponse toMedidaResponse(MedidaAdotada m) {
        if (m == null) return null;
        return MedidaAdotadaResponse.builder()
                .id(m.getId())
                .descricao(m.getDescricao())
                .dataRegistro(m.getDataRegistro())
                .autor(m.getAdmin() != null ? m.getAdmin().getUsuario() : null)
                .build();
    }

    public static List<MedidaAdotadaResponse> toMedidaResponseList(List<MedidaAdotada> medidas) {
        if (medidas == null) return List.of();
        return medidas.stream()
                .map(DenunciaMapper::toMedidaResponse)
                .collect(Collectors.toList());
    }

    public static DenunciaAdminResponse toAdminResponse(Denuncia d) {
        if (d == null) return null;
        return DenunciaAdminResponse.builder()
                .id(d.getId())
                .protocolo(d.getProtocolo())
                .status(d.getEstado())
                .tipo(d.getTipo())
                .unidade(d.getUnidade())
                .dataEnvio(d.getDataAbertura())
                .dataAbertura(extractOpenedAt(d))
                .ultimaAlteracao(d.getUltimaAlteracao())
                .dataFechamento(extractClosedAt(d))
                .dataArquivamento(extractArchivedAt(d))
                .prioridade(d.getPrioridade())
                .build();
    }

    public static DenunciaDetalheResponse toDetalheResponse(Denuncia d, List<MedidaAdotadaResponse> medidas, ConclusaoDenuncia conclusao) {
        if (d == null) return null;
        
        var builder = DenunciaDetalheResponse.builder()
                .id(d.getId())
                .protocolo(d.getProtocolo())
                .status(d.getEstado())
                .tipo(d.getTipo())
                .unidade(d.getUnidade())
                .dataEnvio(d.getDataAbertura())
                .dataAbertura(extractOpenedAt(d))
                .dataFechamento(conclusao != null && conclusao.getTipoConclusao() == TipoConclusao.FINAL ? conclusao.getDataConclusao() : null)
                .dataArquivamento(conclusao != null && conclusao.getTipoConclusao() == TipoConclusao.ARQUIVAMENTO ? conclusao.getDataConclusao() : null)
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
}
