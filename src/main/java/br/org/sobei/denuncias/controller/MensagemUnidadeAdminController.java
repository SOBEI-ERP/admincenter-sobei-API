package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.response.MensagemUnidadeResponse;
import br.org.sobei.denuncias.service.MensagemUnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/mensagens-unidade")
@RequiredArgsConstructor
@Tag(name = "Mensagens de Unidades (Admin)", description = "Gestão de mensagens das unidades por diretoras, coordenadoras e suporte")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('DIRETORA', 'COORDENADORA', 'SUPORTE')")
public class MensagemUnidadeAdminController {

    private final MensagemUnidadeService mensagemUnidadeService;

    @Operation(summary = "Listar mensagens", description = "Lista as mensagens recebidas pela unidade da diretora autenticada ou suporte.")
    @GetMapping
    public ResponseEntity<List<MensagemUnidadeResponse>> listar(
            Principal principal,
            @RequestParam(required = false) String unidade,
            @RequestParam(required = false) Boolean apenasNaoLidas) {

        return ResponseEntity.ok(mensagemUnidadeService.listar(principal.getName(), unidade, apenasNaoLidas));
    }

    @Operation(summary = "Marcar mensagem como lida", description = "Atualiza o status da mensagem para lida.")
    @PatchMapping("/{id}/lida")
    public ResponseEntity<MensagemUnidadeResponse> marcarComoLida(
            @PathVariable Integer id,
            Principal principal) {

        return ResponseEntity.ok(mensagemUnidadeService.marcarComoLida(id, principal.getName()));
    }

    @Operation(summary = "Excluir mensagem", description = "Exclui permanentemente uma mensagem da unidade.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Integer id,
            Principal principal) {

        mensagemUnidadeService.deletar(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
