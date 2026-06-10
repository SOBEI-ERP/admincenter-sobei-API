package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.CriarDenunciaRequest;
import br.org.sobei.denuncias.dto.response.ConsultaProtocoloResponse;
import br.org.sobei.denuncias.dto.response.CriarDenunciaResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.entity.DenuncianteIdentificado;
import br.org.sobei.denuncias.model.entity.MedidaAdotada;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import br.org.sobei.denuncias.repository.DenunciaRepository;
import br.org.sobei.denuncias.repository.MedidaAdotadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe de serviço que contém a regra de negócio principal das denúncias.
 * É responsável por salvar a denúncia no banco, atrelar denunciantes (se houver)
 * e buscar informações baseadas no protocolo.
 */
@Service
@RequiredArgsConstructor
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final ProtocoloService protocoloService;
    private final MedidaAdotadaRepository medidaAdotadaRepository;

    /**
     * Cria uma nova denúncia, gerando um protocolo único no formato ABC-123-456 e salvando os
     * dados validados provenientes da requisição do frontend.
     * Caso o tipo seja IDENTIFICADA, cria também a entidade vinculada DenuncianteIdentificado.
     *
     * @param request DTO com os dados enviados pelo usuário
     * @return DTO com o protocolo gerado para o usuário salvar e acompanhar
     */
    @Transactional
    public CriarDenunciaResponse criarDenuncia(CriarDenunciaRequest request) {
        String protocolo = protocoloService.gerarProtocolo();

        // Constrói a entidade da denúncia, definindo estado padrão inicial como NA_FILA
        Denuncia denuncia = Denuncia.builder()
                .protocolo(protocolo)
                .tipo(request.getTipo())
                .unidade(request.getUnidade())
                .descricao(request.getDescricao())
                .envolvidos(request.getEnvolvidos())
                .testemunhas(request.getTestemunhas())
                .estado(StatusDenuncia.NA_FILA)
                .build();

        // Se a denúncia não for anônima, extrai e vincula os dados de identificação
        if (request.getTipo() == TipoDenuncia.IDENTIFICADA) {
            DenuncianteIdentificado denunciante = DenuncianteIdentificado.builder()
                    .denuncia(denuncia)
                    .nomeCompleto(request.getNomeCompleto())
                    .email(request.getEmail())
                    .telefone(request.getTelefone())
                    .build();
            denuncia.setDenunciante(denunciante);
        }

        // Salva a denúncia (e por ser Cascade, o denunciante também é salvo se existir)
        denunciaRepository.save(denuncia);

        return CriarDenunciaResponse.builder()
                .protocolo(protocolo)
                .mensagem("Denúncia registrada com sucesso.")
                .build();
    }

    /**
     * Busca uma denúncia pelo seu protocolo de forma somente leitura.
     * Transforma a entidade num DTO de resposta limpo que será exposto pela API pública.
     *
     * @param protocolo String contendo o número rastreador da denúncia.
     * @return DTO de resposta com os status atualizados.
     * @throws IllegalArgumentException se o protocolo não existir no banco.
     */
    @Transactional(readOnly = true)
    public ConsultaProtocoloResponse consultarPorProtocolo(String protocolo) {
        Denuncia denuncia = denunciaRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));

        List<String> medidas = medidaAdotadaRepository.findByDenunciaIdOrderByDataRegistroAsc(denuncia.getId())
                .stream().map(MedidaAdotada::getDescricao)
                .collect(Collectors.toList());

        return ConsultaProtocoloResponse.builder()
                .protocolo(denuncia.getProtocolo())
                .estado(denuncia.getEstado())
                .dataAbertura(denuncia.getDataAbertura())
                .ultimaAlteracao(denuncia.getUltimaAlteracao())
                .historicoMedidas(medidas)
                .tipo(denuncia.getTipo())
                .unidade(denuncia.getUnidade())
                .descricao(denuncia.getDescricao())
                .envolvidos(denuncia.getEnvolvidos())
                .testemunhas(denuncia.getTestemunhas())
                .build();
    }
}
