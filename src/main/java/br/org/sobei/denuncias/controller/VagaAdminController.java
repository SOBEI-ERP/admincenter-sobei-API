package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.request.AtualizarVagaRequest;
import br.org.sobei.denuncias.dto.request.CriarVagaRequest;
import br.org.sobei.denuncias.dto.response.CandidaturaResponse;
import br.org.sobei.denuncias.dto.response.VagaResponse;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.enums.StatusVaga;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import br.org.sobei.denuncias.service.CandidaturaService;
import br.org.sobei.denuncias.service.VagaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.org.sobei.denuncias.model.enums.NivelAdmin;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/vagas")
@RequiredArgsConstructor
@Tag(name = "Vagas (Admin/Diretora)", description = "Gerenciamento de vagas por diretoras de unidade")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('DIRETORA', 'SUPORTE')")
public class VagaAdminController {

    private final VagaService vagaService;
    private final CandidaturaService candidaturaService;
    private final UsuarioRepository usuarioRepository;

    @Operation(summary = "Listar vagas da unidade", description = "Lista todas as vagas da unidade da diretora autenticada ou todas se for suporte.")
    @GetMapping
    public ResponseEntity<List<VagaResponse>> listar(
            Principal principal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String unidade) {

        StatusVaga statusVaga = null;
        if (status != null && !status.isBlank()) {
            statusVaga = StatusVaga.fromValue(status);
        }
        return ResponseEntity.ok(vagaService.listar(principal.getName(), statusVaga, unidade));
    }

    @Operation(summary = "Detalhes de uma vaga", description = "Retorna os detalhes completos de uma vaga.")
    @GetMapping("/{id}")
    public ResponseEntity<VagaResponse> buscar(@PathVariable Integer id, Principal principal) {
        return ResponseEntity.ok(vagaService.buscarPorId(id, principal.getName()));
    }

    @Operation(summary = "Criar vaga", description = "Cria uma nova vaga na unidade.")
    @PostMapping
    public ResponseEntity<VagaResponse> criar(@Valid @RequestBody CriarVagaRequest request, Principal principal) {
        return ResponseEntity.ok(vagaService.criar(request, principal.getName()));
    }

    @Operation(summary = "Atualizar vaga", description = "Atualiza os dados de uma vaga existente.")
    @PutMapping("/{id}")
    public ResponseEntity<VagaResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizarVagaRequest request,
            Principal principal) {
        return ResponseEntity.ok(vagaService.atualizar(id, request, principal.getName()));
    }

    @Operation(summary = "Listar candidaturas", description = "Lista todas as candidaturas recebidas em uma vaga.")
    @GetMapping("/{vagaId}/candidaturas")
    public ResponseEntity<List<CandidaturaResponse>> listarCandidaturas(
            @PathVariable Integer vagaId,
            Principal principal) {
        return ResponseEntity.ok(vagaService.listarCandidaturas(vagaId, principal.getName()));
    }

    @Operation(summary = "Download de currículo", description = "Faz o download do currículo de um candidato.")
    @GetMapping("/candidaturas/{candidaturaId}/curriculo")
    public ResponseEntity<Resource> downloadCurriculo(
            @PathVariable Integer candidaturaId,
            Principal principal) {

        // Validar permissão
        Usuario admin = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (admin.getNivel() != NivelAdmin.suporte) {
            String unidadeCandidatura = candidaturaService.getUnidadeDaCandidatura(candidaturaId);
            if (admin.getUnidade() == null || !unidadeCandidatura.equalsIgnoreCase(admin.getUnidade())) {
                throw new IllegalArgumentException("Você não tem permissão para acessar este currículo.");
            }
        }

        Resource resource = candidaturaService.baixarCurriculo(candidaturaId);
        String filename = candidaturaService.getCurriculoNome(candidaturaId);

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
                .body(resource);
    }
}
