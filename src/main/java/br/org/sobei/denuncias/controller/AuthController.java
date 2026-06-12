package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.request.LoginRequest;
import br.org.sobei.denuncias.dto.response.LoginResponse;
import br.org.sobei.denuncias.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação (Admin)", description = "Endpoints para login e geração de tokens JWT dos administradores")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Realizar Login", description = "Valida o usuário e a senha para retornar um Token JWT que dá acesso as rotas protegidas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido e token retornado"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
