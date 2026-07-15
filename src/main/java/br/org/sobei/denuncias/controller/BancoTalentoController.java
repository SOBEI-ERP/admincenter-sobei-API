package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.response.BancoTalentoResponse;
import br.org.sobei.denuncias.dto.response.BancoTalentoVagaResponse;
import br.org.sobei.denuncias.service.BancoTalentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/banco-talentos")
@RequiredArgsConstructor
@Tag(name = "Banco de Talentos", description = "Consulta de candidaturas arquivadas ao fechar vagas")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('DIRETORA', 'SUPORTE')")
public class BancoTalentoController {

    private final BancoTalentoService bancoTalentoService;

    @Operation(summary = "Listar bancos de talentos", description = "Lista os bancos de talentos agrupados por vaga. Suporte pode filtrar por unidade.")
    @GetMapping
    public ResponseEntity<List<BancoTalentoVagaResponse>> listarBancos(
            Principal principal,
            @RequestParam(required = false) String unidade) {
        return ResponseEntity.ok(bancoTalentoService.listarBancos(principal.getName(), unidade));
    }

    @Operation(summary = "Listar talentos de uma vaga", description = "Lista os candidatos arquivados no banco de talentos de uma vaga específica.")
    @GetMapping("/{vagaId}")
    public ResponseEntity<List<BancoTalentoResponse>> listarTalentos(
            @PathVariable Integer vagaId,
            Principal principal) {
        return ResponseEntity.ok(bancoTalentoService.listarTalentosPorVaga(vagaId, principal.getName()));
    }

    @Operation(summary = "Download de currículo do banco de talentos", description = "Faz o download do currículo de um talento armazenado no Cloudflare R2.")
    @GetMapping("/talentos/{talentoId}/curriculo")
    public ResponseEntity<byte[]> downloadCurriculo(
            @PathVariable Integer talentoId,
            Principal principal) {

        byte[] fileContent = bancoTalentoService.baixarCurriculo(talentoId, principal.getName());
        String filename = bancoTalentoService.getCurriculoNome(talentoId);

        String contentType = "application/octet-stream";
        if (filename.toLowerCase().endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (filename.toLowerCase().endsWith(".docx")) {
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (filename.toLowerCase().endsWith(".doc")) {
            contentType = "application/msword";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(fileContent);
    }
}
