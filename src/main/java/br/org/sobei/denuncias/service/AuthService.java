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
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        Usuario user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        String jwtToken = jwtService.generateToken(
                new User(user.getEmail(), user.getSenhaHash(), Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getNivel().name().toUpperCase())))
        );

        return LoginResponse.builder()
                .success(true)
                .token(jwtToken)
                .user(LoginResponse.UserInfo.builder()
                        .usuario(user.getUsuario())
                        .email(user.getEmail())
                        .nivel(user.getNivel().name())
                        .build())
                .build();
    }

    public LoginResponse.UserInfo getAuthenticatedUser(String email) {
        Usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        return LoginResponse.UserInfo.builder()
                .usuario(user.getUsuario())
                .email(user.getEmail())
                .nivel(user.getNivel().name())
                .build();
    }
}
