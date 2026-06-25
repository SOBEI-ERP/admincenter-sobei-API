package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.Candidatura;
import br.org.sobei.denuncias.model.entity.Vaga;
import br.org.sobei.denuncias.model.enums.StatusVaga;
import br.org.sobei.denuncias.repository.CandidaturaRepository;
import br.org.sobei.denuncias.repository.VagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidaturaService {

    private final CandidaturaRepository candidaturaRepository;
    private final VagaRepository vagaRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

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

        if (vaga.getStatus() != StatusVaga.ABERTA) {
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

        // Salvar arquivo em disco
        String originalFilename = curriculo.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir, "curriculos");
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(storedFilename);
            Files.copy(curriculo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Candidatura candidatura = Candidatura.builder()
                    .vaga(vaga)
                    .nomeCompleto(nomeCompleto)
                    .email(email)
                    .telefone(telefone)
                    .cartaApresentacao(cartaApresentacao)
                    .curriculoPath(filePath.toString())
                    .curriculoNome(originalFilename != null ? originalFilename : storedFilename)
                    .build();

            candidaturaRepository.save(candidatura);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o currículo. Tente novamente.", e);
        }
    }

    public Resource baixarCurriculo(Integer candidaturaId) {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new IllegalArgumentException("Candidatura não encontrada."));

        try {
            Path filePath = Paths.get(candidatura.getCurriculoPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Arquivo de currículo não encontrado no servidor.");
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erro ao acessar o arquivo de currículo.", e);
        }
    }

    public String getCurriculoNome(Integer candidaturaId) {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new IllegalArgumentException("Candidatura não encontrada."));
        return candidatura.getCurriculoNome();
    }

    /**
     * Verifica se a candidatura pertence a uma vaga da unidade do admin.
     */
    public String getUnidadeDaCandidatura(Integer candidaturaId) {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new IllegalArgumentException("Candidatura não encontrada."));
        return candidatura.getVaga().getUnidade();
    }
}
