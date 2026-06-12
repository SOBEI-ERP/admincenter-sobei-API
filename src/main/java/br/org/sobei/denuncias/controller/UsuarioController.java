package br.org.sobei.denuncias.controller;

import br.org.sobei.denuncias.dto.request.CreateUsuarioRequest;
import br.org.sobei.denuncias.dto.request.AlterarSenhaRequest;
import br.org.sobei.denuncias.dto.response.UsuarioDto;
import br.org.sobei.denuncias.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários (Admin/Suporte)", description = "Gerenciamento de usuários administradores")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Listar Usuários", description = "Lista todos os usuários administradores cadastrados.")
    @GetMapping
    @PreAuthorize("hasRole('SUPORTE')")
    public ResponseEntity<List<UsuarioDto>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @Operation(summary = "Criar Usuário", description = "Cria um novo usuário administrador/suporte.")
    @PostMapping
    @PreAuthorize("hasRole('SUPORTE')")
    public ResponseEntity<UsuarioDto> criarUsuario(@Valid @RequestBody CreateUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @Operation(summary = "Alterar Senha de Usuário", description = "Altera a senha de um usuário administrador/suporte.")
    @PatchMapping("/{id}/senha")
    @PreAuthorize("hasRole('SUPORTE')")
    public ResponseEntity<Void> alterarSenha(@PathVariable Integer id, @Valid @RequestBody AlterarSenhaRequest request) {
        usuarioService.alterarSenha(id, request.getSenha());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deletar Usuário", description = "Deleta um usuário administrador/suporte do sistema.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPORTE')")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Integer id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
