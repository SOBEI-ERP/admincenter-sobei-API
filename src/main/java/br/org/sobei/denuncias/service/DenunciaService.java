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
import br.org.sobei.denuncias.repository.ConclusaoDenunciaRepository;
import br.org.sobei.denuncias.model.entity.ConclusaoDenuncia;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ConclusaoDenunciaRepository conclusaoDenunciaRepository;

    /**
     * Cria uma nova denúncia, gerando um protocolo único no formato ABC-123-456 e salvando os
     * dados validados provenientes da requisição do frontend.
     * Caso o tipo seja IDENTIFICADA, valida e cria também a entidade vinculada DenuncianteIdentificado.
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
                .unidade(request.getUnidade().trim())
                .descricao(request.getDescricao().trim())
                .envolvidos(request.getEnvolvidos() != null ? request.getEnvolvidos().trim() : null)
                .testemunhas(request.getTestemunhas() != null ? request.getTestemunhas().trim() : null)
                .estado(StatusDenuncia.NA_FILA)
                .build();

        // Se a denúncia for identificada, valida presença obrigatória dos dados de contato
        if (request.getTipo() == TipoDenuncia.IDENTIFICADA) {
            if (request.getNomeCompleto() == null || request.getNomeCompleto().isBlank()
                    || request.getEmail() == null || request.getEmail().isBlank()
                    || request.getTelefone() == null || request.getTelefone().isBlank()) {
                throw new IllegalArgumentException("Para denúncias identificadas, nome completo, e-mail e telefone são obrigatórios.");
            }

            DenuncianteIdentificado denunciante = DenuncianteIdentificado.builder()
                    .denuncia(denuncia)
                    .nomeCompleto(request.getNomeCompleto().trim())
                    .email(request.getEmail().trim().toLowerCase())
                    .telefone(request.getTelefone().trim())
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
        if (protocolo == null || protocolo.isBlank()) {
            throw new IllegalArgumentException("Protocolo inválido ou não informado.");
        }

        Denuncia denuncia = denunciaRepository.findByProtocolo(protocolo.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));

        ConclusaoDenuncia conclusao = conclusaoDenunciaRepository.findById(denuncia.getId()).orElse(null);

        return ConsultaProtocoloResponse.builder()
                .protocolo(denuncia.getProtocolo())
                .estado(denuncia.getEstado())
                .dataAbertura(denuncia.getDataAbertura())
                .ultimaAlteracao(denuncia.getUltimaAlteracao())
                .tipo(denuncia.getTipo())
                .unidade(denuncia.getUnidade())
                .descricao(denuncia.getDescricao())
                .envolvidos(denuncia.getEnvolvidos())
                .testemunhas(denuncia.getTestemunhas())
                .relatorioConclusao(conclusao != null ? conclusao.getRelatorio() : null)
                .tipoConclusao(conclusao != null && conclusao.getTipoConclusao() != null ? conclusao.getTipoConclusao().name() : null)
                .build();
    }
}
