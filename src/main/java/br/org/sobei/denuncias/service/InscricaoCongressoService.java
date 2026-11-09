package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.config.OficinaCotasConfig;
import br.org.sobei.denuncias.dto.request.AtualizarOficinasRequest;
import br.org.sobei.denuncias.dto.request.CriarInscricaoCongressoRequest;
import br.org.sobei.denuncias.dto.response.InscricaoCongressoResponse;
import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import br.org.sobei.denuncias.model.entity.Usuario;
import br.org.sobei.denuncias.model.enums.NivelAdmin;
import br.org.sobei.denuncias.repository.InscricaoCongressoRepository;
import br.org.sobei.denuncias.repository.UsuarioRepository;
import br.org.sobei.denuncias.util.CpfValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InscricaoCongressoService {

    private static final Pattern PATTERN_NAO_DIGITOS = Pattern.compile("\\D+");

    private final InscricaoCongressoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CertificadoCongressoService certificadoService;
    private final CrachaCongressoService crachaService;
    private final EmailService emailService;

    public static final long LIMITE_GERAL_INSCRICOES = 900L;
    public static final int COTA_OUTRAS_OSC_POR_OFICINA = 10;

    // ---- PÚBLICO ----

    @Transactional(readOnly = true)
    public Map<String, Object> obterStatusVagas() {
        long total = inscricaoRepository.count();
        boolean abertas = total < LIMITE_GERAL_INSCRICOES;
        long restantes = Math.max(0, LIMITE_GERAL_INSCRICOES - total);

        return Map.of(
                "totalInscritos", total,
                "limiteMaximo", LIMITE_GERAL_INSCRICOES,
                "vagasRestantes", restantes,
                "inscricoesAbertas", abertas
        );
    }

    @Transactional
    public InscricaoCongressoResponse criar(CriarInscricaoCongressoRequest request) {
        long totalInscritos = inscricaoRepository.count();
        if (totalInscritos >= LIMITE_GERAL_INSCRICOES) {
            throw new IllegalArgumentException("As inscrições para o Congresso estão encerradas. O limite máximo de " + LIMITE_GERAL_INSCRICOES + " participantes foi atingido.");
        }

        String tipoOscLimpo = request.getTipoOsc().trim().toUpperCase();
        if (!tipoOscLimpo.equals("SOBEI") && !tipoOscLimpo.equals("OUTRA")) {
            throw new IllegalArgumentException("Tipo de OSC inválido. Escolha 'SOBEI' ou 'OUTRA'.");
        }

        String unidadeLimpa = null;
        String outraOscLimpa = null;

        if (tipoOscLimpo.equals("SOBEI")) {
            if (request.getUnidade() == null || request.getUnidade().trim().isBlank()) {
                throw new IllegalArgumentException("Selecione a unidade da SOBEI.");
            }
            unidadeLimpa = request.getUnidade().trim();
        } else {
            if (request.getOutraOsc() == null || request.getOutraOsc().trim().isBlank()) {
                throw new IllegalArgumentException("Informe o nome da OSC.");
            }
            outraOscLimpa = request.getOutraOsc().trim();
        }

        // Validação rigorosa de CPF (Numérico ou Alfanumérico da Receita Federal)
        if (!CpfValidator.isValido(request.getCpf())) {
            throw new IllegalArgumentException("CPF inválido. Verifique os dígitos informados.");
        }

        String cpfLimpo = CpfValidator.desformatar(request.getCpf());
        String cpfFormatado = CpfValidator.formatar(request.getCpf());

        if (inscricaoRepository.existsByCpfOrCpfSemMascara(cpfFormatado, cpfLimpo)
                || inscricaoRepository.findByCpf(cpfFormatado).isPresent()
                || inscricaoRepository.findByCpf(cpfLimpo).isPresent()) {
            throw new IllegalArgumentException("Este CPF já está inscrito no Congresso.");
        }

        String emailLimpo = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (emailLimpo.isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }

        if (inscricaoRepository.existsByEmailIgnoreCase(emailLimpo)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado em outra inscrição do Congresso.");
        }

        InscricaoCongresso inscricao = InscricaoCongresso.builder()
                .nomeCompleto(request.getNomeCompleto().trim())
                .cpf(cpfFormatado)
                .email(emailLimpo)
                .tipoOsc(tipoOscLimpo)
                .unidade(unidadeLimpa)
                .outraOsc(outraOscLimpa)
                .presente(false)
                .build();

        InscricaoCongresso salva = inscricaoRepository.save(inscricao);
        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public InscricaoCongressoResponse consultar(String cpf, String email) {
        if (cpf == null || cpf.trim().isBlank() || email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("CPF e e-mail são obrigatórios para a consulta.");
        }

        if (!CpfValidator.isValido(cpf)) {
            throw new IllegalArgumentException("CPF inválido. Verifique os dígitos informados.");
        }

        String cpfLimpo = CpfValidator.desformatar(cpf);
        String cpfFormatado = CpfValidator.formatar(cpf);
        String emailLimpo = email.trim().toLowerCase();

        return inscricaoRepository.findByCpfAndEmail(cpfFormatado, cpfLimpo, emailLimpo)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Nenhuma inscrição encontrada com o CPF e e-mail informados."));
    }

    // ---- ADMIN ----

    @Transactional(readOnly = true)
    public List<InscricaoCongressoResponse> listar(String adminEmail, String termo, String unidade, String tipoOsc, Boolean presente) {
        return listarEntidades(adminEmail, termo, unidade, tipoOsc, presente)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<InscricaoCongressoResponse> listarPaginado(
            String adminEmail, String termo, String unidade, String tipoOsc, Boolean presente, String statusPresenca, int page, int size) {
        Usuario admin = getAdmin(adminEmail);
        if (admin.getNivel() == NivelAdmin.dp) {
            throw new AccessDeniedException("Acesso restrito: departamento pessoal não tem acesso às inscrições do congresso.");
        }

        int pageNum = Math.max(0, page);
        int pageSize = size > 0 ? size : 100;
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "nomeCompleto"));

        final String fTermo = (termo != null && !termo.trim().isBlank()) ? termo.trim().toLowerCase() : null;
        final String rawCpf = (fTermo != null) ? PATTERN_NAO_DIGITOS.matcher(fTermo).replaceAll("") : null;
        final String fTermoCpf = (rawCpf != null && !rawCpf.isBlank()) ? rawCpf : null;

        final String fUnidade = (unidade != null && !unidade.trim().isBlank()) ? unidade.trim() : null;
        final String fTipoOsc = (tipoOsc != null && !tipoOsc.trim().isBlank()) ? tipoOsc.trim() : null;
        final String fStatusPresenca = (statusPresenca != null && !statusPresenca.trim().isBlank()) ? statusPresenca.trim() : null;
        final Boolean fPresente = presente;

        Page<InscricaoCongresso> pageResult;

        if (admin.getNivel() == NivelAdmin.coordenadora || admin.getNivel() == NivelAdmin.diretora) {
            final String fUnidadeAdmin = admin.getUnidade() != null ? admin.getUnidade().trim() : "";
            if (fUnidadeAdmin.isEmpty()) {
                return Page.empty(pageable);
            }
            pageResult = inscricaoRepository.buscarPorUnidadeCoordenadoraPaginado(fUnidadeAdmin, fTermo, fTermoCpf, fStatusPresenca, fPresente, pageable);
        } else if (fTermo == null && fUnidade == null && fTipoOsc == null && fStatusPresenca == null && fPresente == null) {
            pageResult = inscricaoRepository.findAll(pageable);
        } else {
            pageResult = inscricaoRepository.buscarComFiltrosPaginado(fTermo, fTermoCpf, fUnidade, fTipoOsc, fStatusPresenca, fPresente, pageable);
        }

        return pageResult.map(this::toResponse);
    }

    private List<InscricaoCongresso> listarEntidades(String adminEmail, String termo, String unidade, String tipoOsc, Boolean presente) {
        Usuario admin = getAdmin(adminEmail);
        if (admin.getNivel() == NivelAdmin.dp) {
            throw new AccessDeniedException("Acesso restrito: departamento pessoal não tem acesso às inscrições do congresso.");
        }

        final String fTermo = (termo != null && !termo.trim().isBlank()) ? termo.trim().toLowerCase() : null;
        final String rawCpf = (fTermo != null) ? PATTERN_NAO_DIGITOS.matcher(fTermo).replaceAll("") : null;
        final String fTermoCpf = (rawCpf != null && !rawCpf.isBlank()) ? rawCpf : null;

        final String fUnidade = (unidade != null && !unidade.trim().isBlank()) ? unidade.trim() : null;
        final String fTipoOsc = (tipoOsc != null && !tipoOsc.trim().isBlank()) ? tipoOsc.trim() : null;
        final Boolean fPresente = presente;

        // Se for COORDENADORA ou DIRETORA de CEI específica (gestoras de unidade local)
        if (admin.getNivel() == NivelAdmin.coordenadora || admin.getNivel() == NivelAdmin.diretora) {
            final String fUnidadeAdmin = admin.getUnidade() != null ? admin.getUnidade().trim() : "";
            if (fUnidadeAdmin.isEmpty()) {
                return Collections.emptyList();
            }
            return inscricaoRepository.buscarPorUnidadeCoordenadora(fUnidadeAdmin, fTermo, fTermoCpf, fPresente);
        }

        // SUPORTE, CREDENCIADOR, COORDENADORA_EVENTO -> ACESSO GERAL AO EVENTO
        if (fTermo == null && fUnidade == null && fTipoOsc == null && fPresente == null) {
            return inscricaoRepository.findAllByOrderByNomeCompletoAsc();
        }

        return inscricaoRepository.buscarComFiltros(fTermo, fTermoCpf, fUnidade, fTipoOsc, fPresente);
    }

    @Transactional
    public InscricaoCongressoResponse alterarPresenca(Integer id, Integer dia, Boolean presente, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        if (admin.getNivel() != NivelAdmin.suporte &&
            admin.getNivel() != NivelAdmin.credenciador &&
            admin.getNivel() != NivelAdmin.coordenadora_evento) {
            throw new AccessDeniedException("Acesso restrito: apenas credenciadores, coordenação do evento ou suporte podem confirmar presença no congresso.");
        }

        InscricaoCongresso inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        LocalDateTime agora = LocalDateTime.now();

        if (dia != null && dia == 11) {
            boolean novoStatus = presente != null ? presente : !Boolean.TRUE.equals(inscricao.getPresenteDia11());
            inscricao.setPresenteDia11(novoStatus);
            inscricao.setDataPresencaDia11(novoStatus ? agora : null);
        } else if (dia != null && dia == 12) {
            boolean novoStatus = presente != null ? presente : !Boolean.TRUE.equals(inscricao.getPresenteDia12());
            inscricao.setPresenteDia12(novoStatus);
            inscricao.setDataPresencaDia12(novoStatus ? agora : null);
        } else {
            boolean novoStatus = presente != null ? presente : !Boolean.TRUE.equals(inscricao.getPresente());
            inscricao.setPresente(novoStatus);
            inscricao.setDataPresenca(novoStatus ? agora : null);
            inscricao.setPresenteDia11(novoStatus);
            inscricao.setDataPresencaDia11(novoStatus ? agora : null);
            inscricao.setPresenteDia12(novoStatus);
            inscricao.setDataPresencaDia12(novoStatus ? agora : null);
        }

        // Mantém presente = true se compareceu em pelo menos um dia
        boolean compareceuPeloMenosUm = Boolean.TRUE.equals(inscricao.getPresenteDia11())
                || Boolean.TRUE.equals(inscricao.getPresenteDia12())
                || Boolean.TRUE.equals(inscricao.getPresente());
        inscricao.setPresente(compareceuPeloMenosUm);
        if (compareceuPeloMenosUm && inscricao.getDataPresenca() == null) {
            inscricao.setDataPresenca(agora);
        } else if (!compareceuPeloMenosUm) {
            inscricao.setDataPresenca(null);
        }

        InscricaoCongresso salva = inscricaoRepository.save(inscricao);
        return toResponse(salva);
    }

    @Transactional
    public InscricaoCongressoResponse atualizarOficinas(Integer id, AtualizarOficinasRequest request, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        InscricaoCongresso inscricao = buscarInscricaoAutorizada(id, adminEmail);

        String novaOficina = null;
        if (request.getOficina() != null) {
            novaOficina = request.getOficina().trim().isBlank() ? null : request.getOficina().trim();
        } else if (request.getOficinaManha() != null && !request.getOficinaManha().trim().isBlank()) {
            novaOficina = request.getOficinaManha().trim();
        } else if (request.getOficinaTarde() != null && !request.getOficinaTarde().trim().isBlank()) {
            novaOficina = request.getOficinaTarde().trim();
        }

        // Validação de cota máxima da unidade para participantes SOBEI
        // Usuários com nível SUPORTE possuem liberação irrestrita (sem limite de inscrição em nenhuma oficina)
        if (admin.getNivel() != NivelAdmin.suporte && novaOficina != null && "SOBEI".equalsIgnoreCase(inscricao.getTipoOsc())) {
            int cota = OficinaCotasConfig.obterCotaUnidade(novaOficina, inscricao.getUnidade());
            if (cota < 999) {
                String chaveOficinaNova = OficinaCotasConfig.normalizarTexto(novaOficina);
                String chaveUnidade = OficinaCotasConfig.normalizarUnidade(inscricao.getUnidade());

                List<InscricaoCongresso> candidatosSobei = inscricaoRepository.findSobeiComOficinaExcludingId(id);

                long ocupadas = candidatosSobei.stream()
                        .filter(i -> OficinaCotasConfig.normalizarUnidade(i.getUnidade()).equals(chaveUnidade))
                        .filter(i -> correspondeOficina(i, chaveOficinaNova))
                        .count();

                if (ocupadas >= cota) {
                    throw new IllegalArgumentException(
                            "A cota desta oficina para a unidade " + inscricao.getUnidade() +
                            " já foi preenchida (" + ocupadas + "/" + cota + " vagas ocupadas)."
                    );
                }
            }
        }

        // Validação de cota de 10 vagas reservadas para participantes de OUTRAS OSCs (fora SOBEI)
        // Usuários com nível SUPORTE possuem liberação irrestrita
        if (admin.getNivel() != NivelAdmin.suporte && novaOficina != null && !"SOBEI".equalsIgnoreCase(inscricao.getTipoOsc())) {
            String chaveOficinaNova = OficinaCotasConfig.normalizarTexto(novaOficina);

            List<InscricaoCongresso> candidatosOutrasOsc = inscricaoRepository.findOutrasOscComOficinaExcludingId(id);

            long ocupadasOutrasOsc = candidatosOutrasOsc.stream()
                    .filter(i -> correspondeOficina(i, chaveOficinaNova))
                    .count();

            if (ocupadasOutrasOsc >= COTA_OUTRAS_OSC_POR_OFICINA) {
                throw new IllegalArgumentException(
                        "A cota desta oficina para participantes de outras OSCs já foi preenchida (" + ocupadasOutrasOsc + "/" + COTA_OUTRAS_OSC_POR_OFICINA + " vagas ocupadas)."
                );
            }
        }

        if (request.getOficina() != null) {
            String of = request.getOficina().trim().isBlank() ? null : request.getOficina().trim();
            inscricao.setOficina(of);
            inscricao.setOficinaManha(of);
            inscricao.setOficinaTarde(of);
        } else {
            if (request.getOficinaManha() != null) {
                inscricao.setOficinaManha(request.getOficinaManha().trim().isBlank() ? null : request.getOficinaManha().trim());
            }
            if (request.getOficinaTarde() != null) {
                inscricao.setOficinaTarde(request.getOficinaTarde().trim().isBlank() ? null : request.getOficinaTarde().trim());
            }
            if (inscricao.getOficina() == null) {
                inscricao.setOficina(inscricao.getOficinaManha() != null ? inscricao.getOficinaManha() : inscricao.getOficinaTarde());
            }
        }

        InscricaoCongresso salva = inscricaoRepository.save(inscricao);
        return toResponse(salva);
    }

    private boolean correspondeOficina(InscricaoCongresso i, String chaveOficinaNova) {
        String of = i.getOficina() != null ? i.getOficina() : (i.getOficinaManha() != null ? i.getOficinaManha() : i.getOficinaTarde());
        if (of == null || of.isBlank()) return false;
        String chaveOf = OficinaCotasConfig.normalizarTexto(of);
        return !chaveOf.isEmpty() && (chaveOf.equals(chaveOficinaNova) || chaveOf.contains(chaveOficinaNova) || chaveOficinaNova.contains(chaveOf));
    }

    @Transactional(readOnly = true)
    public byte[] gerarCrachaPdf(Integer id, String adminEmail) {
        InscricaoCongresso inscricao = buscarInscricaoAutorizada(id, adminEmail);
        return crachaService.gerarCrachaIndividual(inscricao);
    }

    @Transactional(readOnly = true)
    public byte[] gerarCrachasLotePdf(String adminEmail, String termo, String unidade, String tipoOsc, Boolean presente) {
        List<InscricaoCongresso> inscricoes = listarEntidades(adminEmail, termo, unidade, tipoOsc, presente);
        if (inscricoes.isEmpty()) {
            throw new IllegalArgumentException("Nenhum inscrito encontrado com os filtros selecionados.");
        }

        return crachaService.gerarGradeCrachas(inscricoes);
    }

    @Transactional(readOnly = true)
    public byte[] gerarCertificadoPdf(Integer id, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        InscricaoCongresso inscricao = buscarInscricaoAutorizada(id, adminEmail);

        if (admin.getNivel() == NivelAdmin.credenciador) {
            boolean checkinAmbosDias = Boolean.TRUE.equals(inscricao.getPresenteDia11()) && Boolean.TRUE.equals(inscricao.getPresenteDia12());
            if (!checkinAmbosDias) {
                throw new IllegalArgumentException("Para usuários de nível credenciador, o certificado só fica disponível após a confirmação de check-in em ambos os dias do evento (11 e 12/Set).");
            }
        }

        return certificadoService.gerarCertificadoPdf(inscricao);
    }

    @Transactional(readOnly = true)
    public boolean enviarCertificado(Integer id, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        InscricaoCongresso inscricao = buscarInscricaoAutorizada(id, adminEmail);

        if (admin.getNivel() == NivelAdmin.credenciador) {
            boolean checkinAmbosDias = Boolean.TRUE.equals(inscricao.getPresenteDia11()) && Boolean.TRUE.equals(inscricao.getPresenteDia12());
            if (!checkinAmbosDias) {
                throw new IllegalArgumentException("Para usuários de nível credenciador, o envio de certificado só fica disponível após a confirmação de check-in em ambos os dias do evento (11 e 12/Set).");
            }
        }

        byte[] pdfBytes = certificadoService.gerarCertificadoPdf(inscricao);
        return emailService.enviarCertificadoCongresso(inscricao, pdfBytes);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> enviarCertificadosLoteAmbosDias(String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        if (admin.getNivel() != NivelAdmin.suporte && admin.getNivel() != NivelAdmin.coordenadora_evento) {
            throw new AccessDeniedException("Apenas Suporte ou Coordenação Geral do Evento podem disparar certificados em lote.");
        }

        List<InscricaoCongresso> inscritos = inscricaoRepository.findAllComCheckinAmbosDias();

        if (inscritos.isEmpty()) {
            return Map.of(
                    "success", true,
                    "totalElegiveis", 0,
                    "totalEnviados", 0,
                    "totalFalhas", 0,
                    "message", "Nenhum participante com check-in em ambos os dias (11 e 12/Set) foi encontrado."
            );
        }

        int enviados = 0;
        int falhas = 0;

        for (InscricaoCongresso inscricao : inscritos) {
            if (inscricao.getEmail() == null || inscricao.getEmail().trim().isBlank()) {
                falhas++;
                continue;
            }
            try {
                byte[] pdfBytes = certificadoService.gerarCertificadoPdf(inscricao);
                boolean ok = emailService.enviarCertificadoCongresso(inscricao, pdfBytes);
                if (ok) {
                    enviados++;
                } else {
                    falhas++;
                }
            } catch (Exception e) {
                falhas++;
            }
        }

        return Map.of(
                "success", true,
                "totalElegiveis", inscritos.size(),
                "totalEnviados", enviados,
                "totalFalhas", falhas,
                "message", String.format("Disparo concluído: %d certificado(s) enviado(s) com sucesso para participantes com check-in em ambos os dias (%d falhas/sem e-mail).", enviados, falhas)
        );
    }

    @Transactional
    public void deletarInscricao(Integer id, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        if (admin.getNivel() != NivelAdmin.suporte) {
            throw new AccessDeniedException("Apenas usuários com nível de acesso Suporte podem excluir inscrições do congresso.");
        }

        InscricaoCongresso inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada (ID: " + id + ")."));

        inscricaoRepository.delete(inscricao);
    }

    private InscricaoCongresso buscarInscricaoAutorizada(Integer id, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        if (admin.getNivel() == NivelAdmin.dp) {
            throw new AccessDeniedException("Acesso restrito: departamento pessoal não tem acesso às inscrições do congresso.");
        }
        InscricaoCongresso inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        if (admin.getNivel() == NivelAdmin.coordenadora || admin.getNivel() == NivelAdmin.diretora) {
            if (!"SOBEI".equalsIgnoreCase(inscricao.getTipoOsc()) ||
                    inscricao.getUnidade() == null ||
                    admin.getUnidade() == null ||
                    !inscricao.getUnidade().trim().equalsIgnoreCase(admin.getUnidade().trim())) {
                throw new AccessDeniedException("Acesso restrito à sua própria unidade.");
            }
        }

        return inscricao;
    }

    // ---- HELPERS ----

    private Usuario getAdmin(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    private InscricaoCongressoResponse toResponse(InscricaoCongresso i) {
        return InscricaoCongressoResponse.builder()
                .id(i.getId())
                .nomeCompleto(i.getNomeCompleto())
                .cpf(i.getCpf())
                .email(i.getEmail())
                .tipoOsc(i.getTipoOsc())
                .unidade(i.getUnidade())
                .outraOsc(i.getOutraOsc())
                .presente(i.getPresente())
                .presenteDia11(i.getPresenteDia11())
                .dataPresencaDia11(i.getDataPresencaDia11())
                .presenteDia12(i.getPresenteDia12())
                .dataPresencaDia12(i.getDataPresencaDia12())
                .oficina(i.getOficina() != null ? i.getOficina() : (i.getOficinaManha() != null ? i.getOficinaManha() : i.getOficinaTarde()))
                .oficinaManha(i.getOficinaManha())
                .oficinaTarde(i.getOficinaTarde())
                .dataInscricao(i.getDataInscricao())
                .dataPresenca(i.getDataPresenca())
                .build();
    }
}
