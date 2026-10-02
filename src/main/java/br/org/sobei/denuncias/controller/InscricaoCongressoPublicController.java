package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.request.CriarInscricaoCongressoRequest;
import br.org.sobei.denuncias.dto.response.InscricaoCongressoResponse;
import br.org.sobei.denuncias.service.InscricaoCongressoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/inscricoes-congresso")
@RequiredArgsConstructor
@Tag(name = "Inscrições Congresso (Público)", description = "Endpoint público para inscrição no Congresso de Educação Infantil")
public class InscricaoCongressoPublicController {

    private final InscricaoCongressoService inscricaoService;

    @Operation(summary = "Realizar Inscrição no Congresso", description = "Cadastra uma nova inscrição no congresso SOBEI.")
    @PostMapping
    public ResponseEntity<InscricaoCongressoResponse> criar(@Valid @RequestBody CriarInscricaoCongressoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inscricaoService.criar(request));
    }

    @Operation(summary = "Consultar Inscrição no Congresso", description = "Consulta os dados de uma inscrição através de CPF e e-mail.")
    @PostMapping("/consultar")
    public ResponseEntity<InscricaoCongressoResponse> consultar(@Valid @RequestBody br.org.sobei.denuncias.dto.request.ConsultarInscricaoRequest request) {
        return ResponseEntity.ok(inscricaoService.consultar(request.getCpf(), request.getEmail()));
    }

    @Operation(summary = "Status de Vagas do Congresso", description = "Retorna a disponibilidade geral de vagas e se as inscrições estão abertas (limite de 900).")
    @org.springframework.web.bind.annotation.GetMapping("/status")
    public ResponseEntity<java.util.Map<String, Object>> obterStatusVagas() {
        return ResponseEntity.ok(inscricaoService.obterStatusVagas());
    }
}
