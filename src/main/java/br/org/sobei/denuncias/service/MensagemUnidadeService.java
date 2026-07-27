package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.CriarMensagemUnidadeRequest;
import br.org.sobei.denuncias.dto.response.MensagemUnidadeResponse;
import br.org.sobei.denuncias.model.entity.MensagemUnidade;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.enums.NivelAdmin;
import br.org.sobei.denuncias.repository.MensagemUnidadeRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MensagemUnidadeService {

    private final MensagemUnidadeRepository mensagemUnidadeRepository;
    private final UsuarioRepository usuarioRepository;

    // ---- Público ----

    @Transactional
    public MensagemUnidadeResponse enviar(CriarMensagemUnidadeRequest request) {
        MensagemUnidade mensagem = MensagemUnidade.builder()
                .unidade(request.getUnidade().trim())
                .nomeCompleto(request.getNomeCompleto().trim())
                .email(request.getEmail().trim().toLowerCase())
                .telefone(request.getTelefone().trim())
                .mensagem(request.getMensagem().trim())
                .lida(false)
                .build();

        MensagemUnidade salva = mensagemUnidadeRepository.save(mensagem);
        return toResponse(salva);
    }

    // ---- Admin ----

    @Transactional(readOnly = true)
    public List<MensagemUnidadeResponse> listar(String adminEmail, String unidadeFiltro, Boolean apenasNaoLidas) {
        Usuario admin = getAdmin(adminEmail);

        List<MensagemUnidade> mensagens;
        if (admin.getNivel() == NivelAdmin.suporte) {
            if (unidadeFiltro != null && !unidadeFiltro.isBlank()) {
                if (Boolean.TRUE.equals(apenasNaoLidas)) {
                    mensagens = mensagemUnidadeRepository.findByUnidadeContainingIgnoreCaseAndLidaOrderByDataEnvioDesc(unidadeFiltro, false);
                } else {
                    mensagens = mensagemUnidadeRepository.findByUnidadeContainingIgnoreCaseOrderByDataEnvioDesc(unidadeFiltro);
                }
            } else {
                if (Boolean.TRUE.equals(apenasNaoLidas)) {
                    mensagens = mensagemUnidadeRepository.findByLidaOrderByDataEnvioDesc(false);
                } else {
                    mensagens = mensagemUnidadeRepository.findAllByOrderByDataEnvioDesc();
                }
            }
        } else {
            validarDiretora(admin);
            if (Boolean.TRUE.equals(apenasNaoLidas)) {
                mensagens = mensagemUnidadeRepository.findByUnidadeContainingIgnoreCaseAndLidaOrderByDataEnvioDesc(admin.getUnidade(), false);
            } else {
                mensagens = mensagemUnidadeRepository.findByUnidadeContainingIgnoreCaseOrderByDataEnvioDesc(admin.getUnidade());
            }
        }

        return mensagens.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MensagemUnidadeResponse marcarComoLida(Integer id, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);

        MensagemUnidade mensagem = mensagemUnidadeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensagem não encontrada."));

        if (admin.getNivel() != NivelAdmin.suporte) {
            validarDiretora(admin);
            if (!unidadeCombina(mensagem.getUnidade(), admin.getUnidade())) {
                throw new IllegalArgumentException("Você não tem permissão para alterar mensagens desta unidade.");
            }
        }

        mensagem.setLida(true);
        MensagemUnidade salva = mensagemUnidadeRepository.save(mensagem);
        return toResponse(salva);
    }

    @Transactional
    public void deletar(Integer id, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);

        MensagemUnidade mensagem = mensagemUnidadeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensagem não encontrada."));

        if (admin.getNivel() != NivelAdmin.suporte) {
            validarDiretora(admin);
            if (!unidadeCombina(mensagem.getUnidade(), admin.getUnidade())) {
                throw new IllegalArgumentException("Você não tem permissão para excluir mensagens desta unidade.");
            }
        }

        mensagemUnidadeRepository.delete(mensagem);
    }

    private boolean unidadeCombina(String unidade1, String unidade2) {
        if (unidade1 == null || unidade2 == null) return false;
        String u1 = unidade1.toLowerCase();
        String u2 = unidade2.toLowerCase();
        return u1.contains(u2) || u2.contains(u1);
    }

    // ---- Helpers ----

    private Usuario getAdmin(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    private void validarDiretora(Usuario admin) {
        if (admin.getNivel() != NivelAdmin.diretora) {
            throw new IllegalArgumentException("Acesso restrito ao nível diretora.");
        }
        if (admin.getUnidade() == null || admin.getUnidade().isBlank()) {
            throw new IllegalArgumentException("Diretora sem unidade vinculada. Contacte o suporte.");
        }
    }

    private MensagemUnidadeResponse toResponse(MensagemUnidade m) {
        return MensagemUnidadeResponse.builder()
                .id(m.getId())
                .unidade(m.getUnidade())
                .nomeCompleto(m.getNomeCompleto())
                .email(m.getEmail())
                .telefone(m.getTelefone())
                .mensagem(m.getMensagem())
                .lida(m.getLida())
                .dataEnvio(m.getDataEnvio())
                .build();
    }
}
