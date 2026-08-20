package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.response.InscricaoCongressoResponse;
import br.org.sobei.denuncias.service.InscricaoCongressoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/inscricoes-congresso")
@RequiredArgsConstructor
@Tag(name = "Inscrições Congresso (Admin)", description = "Gestão e credenciamento de inscritos no Congresso")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('CREDENCIADOR', 'COORDENADORA', 'COORDENADORA_EVENTO', 'SUPORTE', 'DP', 'DIRETORA')")
public class InscricaoCongressoAdminController {

    private final InscricaoCongressoService inscricaoService;

    @Operation(summary = "Listar inscritos no Congresso", description = "Lista inscritos com filtros por unidade, OSC, status de presença e busca por nome ou CPF.")
    @GetMapping
    public ResponseEntity<List<InscricaoCongressoResponse>> listar(
            Principal principal,
            @Parameter(description = "Busca por Nome ou CPF") @RequestParam(required = false) String termo,
            @Parameter(description = "Filtrar por Unidade") @RequestParam(required = false) String unidade,
            @Parameter(description = "Filtrar por tipo de OSC (SOBEI ou OUTRA)") @RequestParam(required = false) String tipoOsc,
            @Parameter(description = "Filtrar por presença confirmada (true/false)") @RequestParam(required = false) Boolean presente
    ) {
        return ResponseEntity.ok(inscricaoService.listar(principal.getName(), termo, unidade, tipoOsc, presente));
    }

    @Operation(summary = "Confirmar ou alternar presença de inscrito", description = "Atualiza o status de presença de um inscrito no evento para o dia 11, dia 12 ou geral.")
    @PatchMapping("/{id}/presenca")
    @PreAuthorize("hasAnyRole('SUPORTE', 'DP', 'DIRETORA', 'CREDENCIADOR', 'COORDENADORA_EVENTO')")
    public ResponseEntity<InscricaoCongressoResponse> alterarPresenca(
            @PathVariable Integer id,
            @Parameter(description = "Dia do evento (11 ou 12)") @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Boolean presente,
            Principal principal
    ) {
        return ResponseEntity.ok(inscricaoService.alterarPresenca(id, dia, presente, principal.getName()));
    }

    @Operation(summary = "Enviar certificado por e-mail", description = "Gera o certificado personalizado em PDF e dispara para o e-mail do congressista.")
    @PostMapping("/{id}/enviar-certificado")
    public ResponseEntity<java.util.Map<String, Object>> enviarCertificado(
            @PathVariable Integer id,
            Principal principal
    ) {
        boolean enviado = inscricaoService.enviarCertificado(id, principal.getName());
        return ResponseEntity.ok(java.util.Map.of(
                "success", enviado,
                "message", "Certificado enviado com sucesso para o e-mail do participante.",
                "inscricaoId", id
        ));
    }

    @Operation(summary = "Visualizar ou baixar certificado em PDF", description = "Gera e retorna o arquivo PDF do certificado para visualização ou download direto.")
    @GetMapping(value = "/{id}/certificado", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarCertificado(
            @PathVariable Integer id,
            Principal principal
    ) {
        byte[] pdfBytes = inscricaoService.gerarCertificadoPdf(id, principal.getName());
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.inline()
                .filename("Certificado_Congresso_SOBEI_2026.pdf")
                .build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
