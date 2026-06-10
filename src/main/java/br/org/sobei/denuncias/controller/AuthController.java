package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.config.JwtService;
import br.org.sobei.denuncias.dto.request.LoginRequest;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação (Admin)", description = "Endpoints para login e geração de tokens JWT dos administradores")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Operation(summary = "Realizar Login", description = "Valida o usuário e a senha para retornar um Token JWT que dá acesso as rotas protegidas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido e token retornado"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getSenha())
        );

        Usuario user = usuarioRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        String jwtToken = jwtService.generateToken(
                new User(user.getUsuario(), user.getSenhaHash(), Collections.emptyList())
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", jwtToken);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("usuario", user.getUsuario());
        response.put("user", userInfo);

        return ResponseEntity.ok(response);
    }
}
