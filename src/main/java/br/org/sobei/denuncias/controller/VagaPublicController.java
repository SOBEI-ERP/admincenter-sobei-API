package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.response.VagaPublicResponse;
import br.org.sobei.denuncias.service.CandidaturaService;
import br.org.sobei.denuncias.service.VagaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/vagas")
@RequiredArgsConstructor
@Tag(name = "Vagas (Público)", description = "Listagem pública de vagas e envio de candidaturas")
public class VagaPublicController {

    private final VagaService vagaService;
    private final CandidaturaService candidaturaService;

    @Operation(summary = "Listar vagas abertas", description = "Retorna todas as vagas com status 'aberta' para exibição pública.")
    @GetMapping
    public ResponseEntity<List<VagaPublicResponse>> listar() {
        return ResponseEntity.ok(vagaService.listarPublicas());
    }

    @Operation(summary = "Detalhes de uma vaga", description = "Retorna os detalhes de uma vaga aberta.")
    @GetMapping("/{id}")
    public ResponseEntity<VagaPublicResponse> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(vagaService.buscarPublicaPorId(id));
    }

    @Operation(summary = "Enviar candidatura", description = "Envia uma candidatura para uma vaga com upload de currículo.")
    @PostMapping(value = "/{vagaId}/candidatar", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> candidatar(
            @PathVariable Integer vagaId,
            @RequestParam("nomeCompleto") String nomeCompleto,
            @RequestParam("email") String email,
            @RequestParam("telefone") String telefone,
            @RequestParam(value = "cartaApresentacao", required = false) String cartaApresentacao,
            @RequestParam("curriculo") MultipartFile curriculo) {

        candidaturaService.candidatar(vagaId, nomeCompleto, email, telefone, cartaApresentacao, curriculo);

        return ResponseEntity.ok(Map.of("success", true, "message", "Candidatura enviada com sucesso!"));
    }
}
