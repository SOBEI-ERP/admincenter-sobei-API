package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.request.AtualizarDenunciaRequest;
import br.org.sobei.denuncias.dto.response.DenunciaAdminResponse;
import br.org.sobei.denuncias.dto.response.DenunciaDetalheResponse;
import br.org.sobei.denuncias.service.DenunciaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/denuncias")
@RequiredArgsConstructor
@Tag(name = "Gerenciamento de Denúncias (Admin)", description = "Endpoints administrativos para listagem, detalhamento e fechamento de denúncias.")
@SecurityRequirement(name = "BearerAuth")
public class DenunciaAdminController {

    private final DenunciaAdminService denunciaAdminService;

    @Operation(summary = "Listar denúncias", description = "Traz todas as denúncias ou as filtra de acordo com os query params fornecidos.")
    @GetMapping
    public ResponseEntity<List<DenunciaAdminResponse>> listarDenuncias(
            @Parameter(description = "Filtrar por Status (ex: NA_FILA, EM_ANDAMENTO, FECHADA, ARQUIVADA)") @RequestParam(required = false) String status,
            @Parameter(description = "Filtrar por Tipo (ex: ANONIMA, IDENTIFICADA)") @RequestParam(required = false) String tipo,
            @Parameter(description = "Filtrar por Unidade") @RequestParam(required = false) String unidade,
            @Parameter(description = "Ordem de visualização (ex: recentes, antigas)") @RequestParam(required = false) String ordem
    ) {
        return ResponseEntity.ok(denunciaAdminService.listarDenuncias(status, tipo, unidade, ordem));
    }

    @Operation(summary = "Buscar detalhes de uma denúncia", description = "Traz o relatório completo da denúncia incluindo histórico e medidas tomadas.")
    @GetMapping("/{protocolo}")
    public ResponseEntity<DenunciaDetalheResponse> buscarDetalhes(
            @Parameter(description = "Protocolo da denúncia") @PathVariable String protocolo) {
        return ResponseEntity.ok(denunciaAdminService.buscarDetalhes(protocolo));
    }

    @Operation(summary = "Atualizar Status e Adicionar Medidas", description = "Atualiza o estado da denúncia, registra medidas tomadas e relatórios de conclusão.")
    @PatchMapping("/{protocolo}")
    public ResponseEntity<DenunciaDetalheResponse> atualizarDenuncia(
            @Parameter(description = "Protocolo da denúncia") @PathVariable String protocolo,
            @Valid @RequestBody AtualizarDenunciaRequest request
    ) {
        return ResponseEntity.ok(denunciaAdminService.atualizarDenuncia(protocolo, request));
    }
}
