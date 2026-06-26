package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.AtualizarVagaRequest;
import br.org.sobei.denuncias.dto.request.CriarVagaRequest;
import br.org.sobei.denuncias.dto.response.CandidaturaResponse;
import br.org.sobei.denuncias.dto.response.VagaPublicResponse;
import br.org.sobei.denuncias.dto.response.VagaResponse;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.entity.Vaga;
import br.org.sobei.denuncias.model.enums.NivelAdmin;
import br.org.sobei.denuncias.model.enums.StatusVaga;
import br.org.sobei.denuncias.repository.CandidaturaRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import br.org.sobei.denuncias.repository.VagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VagaService {

    private final VagaRepository vagaRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final UsuarioRepository usuarioRepository;

    // ---- Admin (Diretora) ----

    @Transactional(readOnly = true)
    public List<VagaResponse> listar(String adminEmail, StatusVaga status, String unidade) {
        Usuario admin = getAdmin(adminEmail);
        
        if (admin.getNivel() == NivelAdmin.suporte) {
            List<Vaga> vagas;
            if (unidade != null && !unidade.isBlank()) {
                if (status != null) {
                    vagas = vagaRepository.findByUnidadeAndStatusOrderByDataCriacaoDesc(unidade, status);
                } else {
                    vagas = vagaRepository.findByUnidadeOrderByDataCriacaoDesc(unidade);
                }
            } else {
                if (status != null) {
                    vagas = vagaRepository.findByStatusOrderByDataCriacaoDesc(status);
                } else {
                    vagas = vagaRepository.findAllByOrderByDataCriacaoDesc();
                }
            }
            return vagas.stream().map(this::toResponse).collect(Collectors.toList());
        } else {
            validarDiretora(admin);
            List<Vaga> vagas;
            if (status != null) {
                vagas = vagaRepository.findByUnidadeAndStatusOrderByDataCriacaoDesc(admin.getUnidade(), status);
            } else {
                vagas = vagaRepository.findByUnidadeOrderByDataCriacaoDesc(admin.getUnidade());
            }
            return vagas.stream().map(this::toResponse).collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public VagaResponse buscarPorId(Integer id, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        
        Vaga vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada."));

        if (admin.getNivel() == NivelAdmin.suporte) {
            return toResponse(vaga);
        }

        validarDiretora(admin);
        if (!vaga.getUnidade().equalsIgnoreCase(admin.getUnidade())) {
            throw new IllegalArgumentException("Você não tem permissão para acessar esta vaga.");
        }

        return toResponse(vaga);
    }

    @Transactional
    public VagaResponse criar(CriarVagaRequest request, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        validarDiretora(admin);

        Vaga vaga = Vaga.builder()
                .titulo(request.getTitulo())
                .departamento(request.getDepartamento())
                .unidade(admin.getUnidade())
                .descricao(request.getDescricao())
                .requisitos(request.getRequisitos())
                .beneficios(request.getBeneficios())
                .modalidade(request.getModalidade())
                .tipoContrato(request.getTipoContrato())
                .status(StatusVaga.ATIVO)
                .admin(admin)
                .build();

        Vaga salva = vagaRepository.save(vaga);
        return toResponse(salva);
    }

    @Transactional
    public VagaResponse atualizar(Integer id, AtualizarVagaRequest request, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        validarDiretora(admin);

        Vaga vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada."));

        if (!vaga.getUnidade().equalsIgnoreCase(admin.getUnidade())) {
            throw new IllegalArgumentException("Você não tem permissão para editar esta vaga.");
        }

        vaga.setTitulo(request.getTitulo());
        vaga.setDepartamento(request.getDepartamento());
        vaga.setDescricao(request.getDescricao());
        vaga.setRequisitos(request.getRequisitos());
        vaga.setBeneficios(request.getBeneficios());
        vaga.setModalidade(request.getModalidade());
        vaga.setTipoContrato(request.getTipoContrato());
        vaga.setStatus(request.getStatus());

        Vaga salva = vagaRepository.save(vaga);
        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<CandidaturaResponse> listarCandidaturas(Integer vagaId, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        
        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada."));

        if (admin.getNivel() != NivelAdmin.suporte) {
            validarDiretora(admin);
            if (!vaga.getUnidade().equalsIgnoreCase(admin.getUnidade())) {
                throw new IllegalArgumentException("Você não tem permissão para acessar candidaturas desta vaga.");
            }
        }

        return candidaturaRepository.findByVagaIdOrderByDataEnvioDesc(vagaId)
                .stream()
                .map(c -> CandidaturaResponse.builder()
                        .id(c.getId())
                        .nomeCompleto(c.getNomeCompleto())
                        .email(c.getEmail())
                        .telefone(c.getTelefone())
                        .cartaApresentacao(c.getCartaApresentacao())
                        .curriculoNome(c.getCurriculoNome())
                        .dataEnvio(c.getDataEnvio())
                        .build())
                .collect(Collectors.toList());
    }

    // ---- Público ----

    @Transactional(readOnly = true)
    public List<VagaPublicResponse> listarPublicas() {
        return vagaRepository.findByStatusOrderByDataCriacaoDesc(StatusVaga.ATIVO)
                .stream()
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VagaPublicResponse buscarPublicaPorId(Integer id) {
        Vaga vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada."));

        return toPublicResponse(vaga);
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

    private VagaResponse toResponse(Vaga vaga) {
        return VagaResponse.builder()
                .id(vaga.getId())
                .titulo(vaga.getTitulo())
                .departamento(vaga.getDepartamento())
                .unidade(vaga.getUnidade())
                .descricao(vaga.getDescricao())
                .requisitos(vaga.getRequisitos())
                .beneficios(vaga.getBeneficios())
                .modalidade(vaga.getModalidade())
                .tipoContrato(vaga.getTipoContrato())
                .status(vaga.getStatus())
                .totalCandidaturas((int) candidaturaRepository.countByVagaId(vaga.getId()))
                .dataCriacao(vaga.getDataCriacao())
                .dataAtualizacao(vaga.getDataAtualizacao())
                .build();
    }

    private VagaPublicResponse toPublicResponse(Vaga vaga) {
        return VagaPublicResponse.builder()
                .id(vaga.getId())
                .titulo(vaga.getTitulo())
                .departamento(vaga.getDepartamento())
                .unidade(vaga.getUnidade())
                .descricao(vaga.getDescricao())
                .requisitos(vaga.getRequisitos())
                .beneficios(vaga.getBeneficios())
                .modalidade(vaga.getModalidade())
                .tipoContrato(vaga.getTipoContrato())
                .status(vaga.getStatus())
                .dataCriacao(vaga.getDataCriacao())
                .build();
    }
}
