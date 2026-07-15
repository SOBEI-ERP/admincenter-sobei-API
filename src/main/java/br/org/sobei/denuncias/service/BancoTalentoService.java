package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.response.BancoTalentoResponse;
import br.org.sobei.denuncias.dto.response.BancoTalentoVagaResponse;
import br.org.sobei.denuncias.model.entity.BancoTalento;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.enums.NivelAdmin;
import br.org.sobei.denuncias.repository.BancoTalentoRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BancoTalentoService {

    private final BancoTalentoRepository bancoTalentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public List<BancoTalentoVagaResponse> listarBancos(String adminEmail, String unidadeFiltro) {
        Usuario admin = getAdmin(adminEmail);

        List<BancoTalento> talentos;
        if (admin.getNivel() == NivelAdmin.suporte) {
            if (unidadeFiltro != null && !unidadeFiltro.isBlank()) {
                talentos = bancoTalentoRepository.findByVagaUnidadeOrderByDataMovimentacaoDesc(unidadeFiltro);
            } else {
                talentos = bancoTalentoRepository.findAll();
            }
        } else {
            validarDiretora(admin);
            talentos = bancoTalentoRepository.findByVagaUnidadeOrderByDataMovimentacaoDesc(admin.getUnidade());
        }

        // Agrupar por vaga e construir os responses
        Map<Integer, List<BancoTalento>> agrupados = talentos.stream()
                .collect(Collectors.groupingBy(t -> t.getVaga().getId()));

        return agrupados.entrySet().stream()
                .map(entry -> {
                    List<BancoTalento> lista = entry.getValue();
                    BancoTalento primeiro = lista.get(0);
                    return BancoTalentoVagaResponse.builder()
                            .vagaId(primeiro.getVaga().getId())
                            .vagaTitulo(primeiro.getVaga().getTitulo())
                            .vagaUnidade(primeiro.getVaga().getUnidade())
                            .totalTalentos(lista.size())
                            .ultimaMovimentacao(lista.stream()
                                    .map(BancoTalento::getDataMovimentacao)
                                    .max(Comparator.naturalOrder())
                                    .orElse(null))
                            .build();
                })
                .sorted(Comparator.comparing(BancoTalentoVagaResponse::getUltimaMovimentacao,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BancoTalentoResponse> listarTalentosPorVaga(Integer vagaId, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);

        List<BancoTalento> talentos = bancoTalentoRepository.findByVagaIdOrderByDataEnvioOriginalDesc(vagaId);

        // Validar permissão por unidade
        if (admin.getNivel() != NivelAdmin.suporte) {
            validarDiretora(admin);
            if (!talentos.isEmpty()) {
                String unidadeVaga = talentos.get(0).getVaga().getUnidade();
                if (!unidadeVaga.equalsIgnoreCase(admin.getUnidade())) {
                    throw new IllegalArgumentException("Você não tem permissão para acessar este banco de talentos.");
                }
            }
        }

        return talentos.stream()
                .map(t -> BancoTalentoResponse.builder()
                        .id(t.getId())
                        .nomeCompleto(t.getNomeCompleto())
                        .email(t.getEmail())
                        .telefone(t.getTelefone())
                        .cartaApresentacao(t.getCartaApresentacao())
                        .curriculoNome(t.getCurriculoNome())
                        .dataEnvioOriginal(t.getDataEnvioOriginal())
                        .dataMovimentacao(t.getDataMovimentacao())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] baixarCurriculo(Integer talentoId, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);

        BancoTalento talento = bancoTalentoRepository.findById(talentoId)
                .orElseThrow(() -> new IllegalArgumentException("Talento não encontrado."));

        if (admin.getNivel() != NivelAdmin.suporte) {
            validarDiretora(admin);
            if (!talento.getVaga().getUnidade().equalsIgnoreCase(admin.getUnidade())) {
                throw new IllegalArgumentException("Você não tem permissão para acessar este currículo.");
            }
        }

        return storageService.download(talento.getCurriculoPath());
    }

    @Transactional(readOnly = true)
    public String getCurriculoNome(Integer talentoId) {
        BancoTalento talento = bancoTalentoRepository.findById(talentoId)
                .orElseThrow(() -> new IllegalArgumentException("Talento não encontrado."));
        return talento.getCurriculoNome();
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
}
