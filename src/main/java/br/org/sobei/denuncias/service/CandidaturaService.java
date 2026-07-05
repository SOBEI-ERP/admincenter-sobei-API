package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.Candidatura;
import br.org.sobei.denuncias.model.entity.Vaga;
import br.org.sobei.denuncias.model.enums.StatusVaga;
import br.org.sobei.denuncias.repository.CandidaturaRepository;
import br.org.sobei.denuncias.repository.VagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CandidaturaService {

    private final CandidaturaRepository candidaturaRepository;
    private final VagaRepository vagaRepository;
    private final StorageService storageService;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Transactional
    public void candidatar(Integer vagaId, String nomeCompleto, String email,
                           String telefone, String cartaApresentacao, MultipartFile curriculo) {

        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada."));

        if (vaga.getStatus() != StatusVaga.ATIVO) {
            throw new IllegalArgumentException("Esta vaga não está mais aceitando candidaturas.");
        }

        // Validações do arquivo
        if (curriculo == null || curriculo.isEmpty()) {
            throw new IllegalArgumentException("O currículo é obrigatório.");
        }

        if (curriculo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("O arquivo não deve exceder 5MB.");
        }

        String contentType = curriculo.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Apenas arquivos PDF, DOC ou DOCX são aceitos.");
        }

        // Upload para o Cloudflare R2
        String key = storageService.upload(curriculo, "curriculos");

        String originalFilename = curriculo.getOriginalFilename();

        Candidatura candidatura = Candidatura.builder()
                .vaga(vaga)
                .nomeCompleto(nomeCompleto)
                .email(email)
                .telefone(telefone)
                .cartaApresentacao(cartaApresentacao)
                .curriculoPath(key)
                .curriculoNome(originalFilename != null ? originalFilename : key)
                .build();

        candidaturaRepository.save(candidatura);
    }

    /**
     * Baixa o conteúdo do currículo armazenado no Cloudflare R2.
     *
     * @param candidaturaId ID da candidatura
     * @return Conteúdo do arquivo em bytes
     */
    @Transactional(readOnly = true)
    public byte[] baixarCurriculo(Integer candidaturaId) {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new IllegalArgumentException("Candidatura não encontrada."));

        return storageService.download(candidatura.getCurriculoPath());
    }

    @Transactional(readOnly = true)
    public String getCurriculoNome(Integer candidaturaId) {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new IllegalArgumentException("Candidatura não encontrada."));
        return candidatura.getCurriculoNome();
    }

    /**
     * Verifica se a candidatura pertence a uma vaga da unidade do admin.
     */
    @Transactional(readOnly = true)
    public String getUnidadeDaCandidatura(Integer candidaturaId) {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new IllegalArgumentException("Candidatura não encontrada."));
        return candidatura.getVaga().getUnidade();
    }
}

