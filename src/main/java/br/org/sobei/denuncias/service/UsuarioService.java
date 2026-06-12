package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.CreateUsuarioRequest;
import br.org.sobei.denuncias.dto.response.UsuarioDto;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioDto> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UsuarioDto criar(CreateUsuarioRequest request) {
        if (usuarioRepository.findByUsuario(request.getUsuario()).isPresent()) {
            throw new IllegalArgumentException("Nome de usuário já existe.");
        }

        Usuario novoUsuario = Usuario.builder()
                .usuario(request.getUsuario())
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .nivel(request.getNivel())
                .build();

        Usuario salvo = usuarioRepository.save(novoUsuario);
        return toDto(salvo);
    }

    public void deletar(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        usuarioRepository.deleteById(id);
    }

    public void alterarSenha(Integer id, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    private UsuarioDto toDto(Usuario usuario) {
        return UsuarioDto.builder()
                .id(usuario.getId())
                .usuario(usuario.getUsuario())
                .nivel(usuario.getNivel())
                .build();
    }
}

