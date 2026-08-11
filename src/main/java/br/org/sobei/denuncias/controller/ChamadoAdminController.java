package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.request.AtualizarChamadoRequest;
import br.org.sobei.denuncias.dto.request.CriarChamadoRequest;
import br.org.sobei.denuncias.dto.response.ChamadoResponse;
import br.org.sobei.denuncias.model.enums.PrioridadeChamado;
import br.org.sobei.denuncias.model.enums.StatusChamado;
import br.org.sobei.denuncias.service.ChamadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/chamados")
@RequiredArgsConstructor
@Tag(name = "Chamados de Suporte (Admin)", description = "Gerenciamento de chamados de suporte técnico exclusivo para nível Suporte")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasRole('SUPORTE')")
public class ChamadoAdminController {

    private final ChamadoService chamadoService;

    @Operation(summary = "Criar chamado", description = "Registra um novo chamado de suporte (exclusivo para Suporte).")
    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(
            @Valid @RequestBody CriarChamadoRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.criar(request, principal.getName()));
    }

    @Operation(summary = "Listar chamados", description = "Lista os chamados de suporte com filtros opcionais por status e prioridade.")
    @GetMapping
    public ResponseEntity<List<ChamadoResponse>> listar(
            Principal principal,
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) PrioridadeChamado prioridade) {
        return ResponseEntity.ok(chamadoService.listar(principal.getName(), status, prioridade));
    }

    @Operation(summary = "Buscar chamado por ID", description = "Retorna os detalhes de um chamado de suporte específico.")
    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponse> buscarPorId(
            @PathVariable Integer id,
            Principal principal) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id, principal.getName()));
    }

    @Operation(summary = "Atualizar chamado", description = "Atualiza dados, prioridade, status ou resolução de um chamado.")
    @PatchMapping("/{id}")
    public ResponseEntity<ChamadoResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody AtualizarChamadoRequest request,
            Principal principal) {
        return ResponseEntity.ok(chamadoService.atualizar(id, request, principal.getName()));
    }

    @Operation(summary = "Excluir chamado", description = "Exclui um chamado de suporte.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Integer id,
            Principal principal) {
        chamadoService.deletar(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
