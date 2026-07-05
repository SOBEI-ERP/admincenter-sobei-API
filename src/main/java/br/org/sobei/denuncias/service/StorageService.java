package br.org.sobei.denuncias.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

/**
 * Service responsável por operações de upload, download e exclusão de arquivos
 * no Cloudflare R2 via API S3-compatible.
 *
 * Todos os arquivos são armazenados no bucket configurado em {@code app.r2.bucket-name}
 * e organizados por subpastas (ex: "curriculos/uuid-arquivo.pdf").
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final S3Client s3Client;

    @Value("${app.r2.bucket-name}")
    private String bucketName;

    /**
     * Faz upload de um arquivo para o R2 e retorna a key (caminho) no bucket.
     *
     * @param file    O arquivo enviado via multipart
     * @param subPath Subpasta dentro do bucket (ex: "curriculos")
     * @return A key completa do objeto no bucket (ex: "curriculos/abc-123.pdf")
     */
    public String upload(MultipartFile file, String subPath) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String key = subPath + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Arquivo enviado para R2 com sucesso: {}", key);
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo para upload.", e);
        } catch (S3Exception e) {
            log.error("Erro ao enviar arquivo para o R2: {}", e.getMessage());
            throw new RuntimeException("Erro ao armazenar o arquivo. Tente novamente.", e);
        }
    }

    /**
     * Faz download do conteúdo de um arquivo armazenado no R2.
     *
     * @param key A key (caminho) do objeto no bucket
     * @return O conteúdo do arquivo em bytes
     */
    public byte[] download(String key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(getRequest).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("Arquivo não encontrado no storage.");
        } catch (S3Exception e) {
            log.error("Erro ao baixar arquivo do R2: {}", e.getMessage());
            throw new RuntimeException("Erro ao acessar o arquivo. Tente novamente.", e);
        }
    }

    /**
     * Remove um arquivo do R2.
     *
     * @param key A key (caminho) do objeto no bucket
     */
    public void delete(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Arquivo removido do R2: {}", key);
        } catch (S3Exception e) {
            log.error("Erro ao remover arquivo do R2: {}", e.getMessage());
            // Não lança exceção para delete — operação idempotente
        }
    }

    /**
     * Verifica se um arquivo existe no R2.
     *
     * @param key A key (caminho) do objeto no bucket
     * @return true se o arquivo existe, false caso contrário
     */
    public boolean exists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Erro ao verificar existência do arquivo no R2: {}", e.getMessage());
            return false;
        }
    }
}
