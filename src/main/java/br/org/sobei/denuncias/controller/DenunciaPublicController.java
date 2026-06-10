package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.request.CriarDenunciaRequest;
import br.org.sobei.denuncias.dto.response.ConsultaProtocoloResponse;
import br.org.sobei.denuncias.dto.response.CriarDenunciaResponse;
import br.org.sobei.denuncias.service.DenunciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável pelos endpoints públicos (abertos) da API de Denúncias.
 * Permite que qualquer usuário crie uma nova denúncia ou consulte o status
 * de uma denúncia existente utilizando apenas o protocolo.
 */
@RestController
@RequestMapping("/api/public/denuncias")
@RequiredArgsConstructor
@Tag(name = "Denúncias Públicas", description = "Endpoints de acesso público para criação e consulta de denúncias")
public class DenunciaPublicController {

    private final DenunciaService denunciaService;

    /**
     * Endpoint para criar uma nova denúncia no sistema.
     * Recebe os dados de forma validada e gera um protocolo único de acompanhamento.
     *
     * @param request Dados da denúncia, validado pelas anotações (ex: @NotBlank)
     * @return Objeto contendo o protocolo gerado e a mensagem de sucesso
     */
    @Operation(summary = "Registrar uma nova denúncia", description = "Cria uma nova denúncia anônima ou identificada e retorna o número do protocolo gerado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Denúncia registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos campos enviados")
    })
    @PostMapping
    public ResponseEntity<CriarDenunciaResponse> criarDenuncia(
            @Valid @RequestBody CriarDenunciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(denunciaService.criarDenuncia(request));
    }

    /**
     * Endpoint para consultar o status de uma denúncia específica utilizando
     * o número de protocolo fornecido no momento da criação.
     *
     * @param protocolo Número de protocolo gerado (ex: ABC-123-456)
     * @return Status atual, datas e histórico da denúncia
     */
    @Operation(summary = "Consultar denúncia por protocolo", description = "Recupera o status atual, datas de alteração e histórico de uma denúncia usando o protocolo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Protocolo encontrado"),
            @ApiResponse(responseCode = "400", description = "Protocolo inválido ou não encontrado")
    })
    @GetMapping("/protocolo/{protocolo}")
    public ResponseEntity<ConsultaProtocoloResponse> consultarPorProtocolo(
            @Parameter(description = "Número do protocolo da denúncia", required = true)
            @PathVariable String protocolo) {
        return ResponseEntity.ok(denunciaService.consultarPorProtocolo(protocolo));
    }
}
