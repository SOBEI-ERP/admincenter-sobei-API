package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.request.CriarMensagemUnidadeRequest;
import br.org.sobei.denuncias.dto.response.MensagemUnidadeResponse;
import br.org.sobei.denuncias.service.MensagemUnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/mensagens-unidade")
@RequiredArgsConstructor
@Tag(name = "Mensagens de Unidades (Público)", description = "Endpoint público para envio de mensagens/intenções de vaga enviadas pelo sobei.org.br")
public class MensagemUnidadePublicController {

    private final MensagemUnidadeService mensagemUnidadeService;

    @Operation(summary = "Enviar mensagem para unidade", description = "Envia uma mensagem ou intenção de vaga vinda da página pública da unidade.")
    @PostMapping
    public ResponseEntity<MensagemUnidadeResponse> enviar(@Valid @RequestBody CriarMensagemUnidadeRequest request) {
        return ResponseEntity.ok(mensagemUnidadeService.enviar(request));
    }
}
