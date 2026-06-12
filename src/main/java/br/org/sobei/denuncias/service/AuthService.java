package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.config.JwtService;
import br.org.sobei.denuncias.dto.request.LoginRequest;
import br.org.sobei.denuncias.dto.response.LoginResponse;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getSenha())
        );

        Usuario user = usuarioRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        String jwtToken = jwtService.generateToken(
                new User(user.getUsuario(), user.getSenhaHash(), Collections.emptyList())
        );

        return LoginResponse.builder()
                .success(true)
                .token(jwtToken)
                .user(LoginResponse.UserInfo.builder()
                        .usuario(user.getUsuario())
                        .build())
                .build();
    }
}
