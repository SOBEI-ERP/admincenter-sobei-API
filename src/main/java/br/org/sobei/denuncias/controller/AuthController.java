package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.request.LoginRequest;
import br.org.sobei.denuncias.dto.response.LoginResponse;
import br.org.sobei.denuncias.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação (Admin)", description = "Endpoints para login e geração de tokens JWT dos administradores")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Realizar Login", description = "Valida o usuário e a senha para retornar um Token JWT via cookie httpOnly que dá acesso as rotas protegidas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido e token retornado via cookie"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("sobei_token", loginResponse.getToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        loginResponse.setToken(null);

        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Realizar Logout", description = "Remove o cookie de autenticação do navegador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout bem-sucedido")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("sobei_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("success", true));
    }

    @Operation(summary = "Obter Usuário Autenticado", description = "Retorna as informações do usuário autenticado a partir do token JWT.")
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<LoginResponse.UserInfo> me(Principal principal) {
        return ResponseEntity.ok(authService.getAuthenticatedUser(principal.getName()));
    }
}
