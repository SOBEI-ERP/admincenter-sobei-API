package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.response.EstatisticaResponse;
import br.org.sobei.denuncias.service.EstatisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/estatisticas")
@RequiredArgsConstructor
@Tag(name = "Estatísticas (Admin)", description = "Endpoints para os gráficos e painéis estatísticos do Dashboard.")
@SecurityRequirement(name = "BearerAuth")
public class EstatisticaController {

    private final EstatisticaService estatisticaService;

    @Operation(summary = "Obter estatísticas consolidadas", description = "Vare as denúncias do banco e agrupa os totais por tipo, status e unidade.")
    @GetMapping
    public ResponseEntity<EstatisticaResponse> getEstatisticas(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String unidade,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim
    ) {
        return ResponseEntity.ok(estatisticaService.obterEstatisticas(tipo, unidade, dataInicio, dataFim));
    }
}
