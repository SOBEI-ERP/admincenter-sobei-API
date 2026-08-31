package br.org.sobei.denuncias.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscricaoCongressoService {

    private final InscricaoCongressoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CertificadoCongressoService certificadoService;
    private final CrachaCongressoService crachaService;
    private final EmailService emailService;

    // ---- PÚBLICO ----

    @Transactional
    public InscricaoCongressoResponse criar(CriarInscricaoCongressoRequest request) {
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

        if (inscricaoRepository.findByCpf(cpfFormatado).isPresent() || inscricaoRepository.findByCpf(cpfLimpo).isPresent()) {
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
        Usuario admin = getAdmin(adminEmail);

        final String fTermo = (termo != null && !termo.trim().isBlank()) ? termo.trim().toLowerCase() : null;
        String rawCpf = (termo != null) ? termo.replaceAll("\\D", "") : null;
        final String fTermoCpf = (rawCpf != null && !rawCpf.isBlank()) ? rawCpf : null;

        final String fUnidade = (unidade != null && !unidade.trim().isBlank()) ? unidade.trim().toLowerCase() : null;
        final String fTipoOsc = (tipoOsc != null && !tipoOsc.trim().isBlank()) ? tipoOsc.trim().toLowerCase() : null;
        final Boolean fPresente = presente;

        List<InscricaoCongresso> todas = inscricaoRepository.findAllByOrderByNomeCompletoAsc();

        // Se for COORDENADORA de CEI específica (e não suporte/diretoria/credenciamento)
        if (admin.getNivel() == NivelAdmin.coordenadora) {
            final String fUnidadeAdmin = admin.getUnidade() != null ? admin.getUnidade().trim().toLowerCase() : "";
            return todas.stream()
                    .filter(i -> "sobei".equalsIgnoreCase(i.getTipoOsc()))
                    .filter(i -> i.getUnidade() != null && i.getUnidade().trim().equalsIgnoreCase(fUnidadeAdmin))
                    .filter(i -> filtrarInscricao(i, fTermo, fTermoCpf, fUnidade, fTipoOsc, fPresente))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        // SUPORTE, DP, DIRETORA, CREDENCIADOR, COORDENADORA_EVENTO -> ACESSO GERAL IRRESTRITO A TUDO
        return todas.stream()
                .filter(i -> filtrarInscricao(i, fTermo, fTermoCpf, fUnidade, fTipoOsc, fPresente))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean filtrarInscricao(InscricaoCongresso i, String termo, String termoCpf, String unidade, String tipoOsc, Boolean presente) {
        if (termo != null) {
            boolean matchNome = i.getNomeCompleto() != null && i.getNomeCompleto().toLowerCase().contains(termo);
            boolean matchEmail = i.getEmail() != null && i.getEmail().toLowerCase().contains(termo);
            boolean matchCpf = i.getCpf() != null && i.getCpf().contains(termo);
            boolean matchCpfLimpo = false;
            if (termoCpf != null && i.getCpf() != null) {
                String cpfLimpo = i.getCpf().replaceAll("\\D", "");
                matchCpfLimpo = cpfLimpo.contains(termoCpf);
            }
            if (!matchNome && !matchEmail && !matchCpf && !matchCpfLimpo) {
                return false;
            }
        }

        if (unidade != null) {
            if (i.getUnidade() == null || !i.getUnidade().trim().equalsIgnoreCase(unidade)) {
                return false;
            }
        }

        if (tipoOsc != null) {
            if (i.getTipoOsc() == null || !i.getTipoOsc().trim().equalsIgnoreCase(tipoOsc)) {
                return false;
            }
        }

        if (presente != null) {
            boolean isInscritoPresente = Boolean.TRUE.equals(i.getPresente());
            if (isInscritoPresente != presente) {
                return false;
            }
        }

        return true;
    }

    @Transactional
    public InscricaoCongressoResponse alterarPresenca(Integer id, Integer dia, Boolean presente, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);

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
        boolean compareceuPeloMenosUm = Boolean.TRUE.equals(inscricao.getPresenteDia11()) || Boolean.TRUE.equals(inscricao.getPresenteDia12()) || Boolean.TRUE.equals(inscricao.getPresente());
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
        InscricaoCongresso inscricao = buscarInscricaoAutorizada(id, adminEmail);

        if (request.getOficinaManha() != null) {
            inscricao.setOficinaManha(request.getOficinaManha().trim().isBlank() ? null : request.getOficinaManha().trim());
        }
        if (request.getOficinaTarde() != null) {
            inscricao.setOficinaTarde(request.getOficinaTarde().trim().isBlank() ? null : request.getOficinaTarde().trim());
        }

        InscricaoCongresso salva = inscricaoRepository.save(inscricao);
        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public byte[] gerarCrachaPdf(Integer id, String adminEmail) {
        InscricaoCongresso inscricao = buscarInscricaoAutorizada(id, adminEmail);
        return crachaService.gerarCrachaIndividual(inscricao);
    }

    @Transactional(readOnly = true)
    public byte[] gerarCrachasLotePdf(String adminEmail, String termo, String unidade, String tipoOsc, Boolean presente) {
        List<InscricaoCongressoResponse> listaFiltrada = listar(adminEmail, termo, unidade, tipoOsc, presente);
        if (listaFiltrada.isEmpty()) {
            throw new IllegalArgumentException("Nenhum inscrito encontrado com os filtros selecionados.");
        }

        List<Integer> ids = listaFiltrada.stream().map(InscricaoCongressoResponse::getId).collect(Collectors.toList());
        List<InscricaoCongresso> inscricoes = inscricaoRepository.findAllById(ids);

        // Manter a ordenação alfabética
        inscricoes.sort((a, b) -> a.getNomeCompleto().compareToIgnoreCase(b.getNomeCompleto()));

        return crachaService.gerarGradeCrachas(inscricoes);
    }

    @Transactional(readOnly = true)
    public byte[] gerarCertificadoPdf(Integer id, String adminEmail) {
        InscricaoCongresso inscricao = buscarInscricaoAutorizada(id, adminEmail);
        return certificadoService.gerarCertificadoPdf(inscricao);
    }

    @Transactional(readOnly = true)
    public boolean enviarCertificado(Integer id, String adminEmail) {
        InscricaoCongresso inscricao = buscarInscricaoAutorizada(id, adminEmail);
        byte[] pdfBytes = certificadoService.gerarCertificadoPdf(inscricao);
        return emailService.enviarCertificadoCongresso(inscricao, pdfBytes);
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> enviarCertificadosLoteAmbosDias(String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        List<InscricaoCongresso> inscritos;

        if (admin.getNivel() == NivelAdmin.coordenadora) {
            String unidade = admin.getUnidade() != null ? admin.getUnidade().trim() : "";
            inscritos = inscricaoRepository.findAllComCheckinAmbosDiasPorUnidade(unidade);
        } else {
            inscritos = inscricaoRepository.findAllComCheckinAmbosDias();
        }

        if (inscritos.isEmpty()) {
            return java.util.Map.of(
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

        return java.util.Map.of(
                "success", true,
                "totalElegiveis", inscritos.size(),
                "totalEnviados", enviados,
                "totalFalhas", falhas,
                "message", String.format("Disparo concluído: %d certificado(s) enviado(s) com sucesso para participantes com check-in em ambos os dias (%d falhas/sem e-mail).", enviados, falhas)
        );
    }

    private InscricaoCongresso buscarInscricaoAutorizada(Integer id, String adminEmail) {
        Usuario admin = getAdmin(adminEmail);
        InscricaoCongresso inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        if (admin.getNivel() == NivelAdmin.coordenadora) {
            if (!"SOBEI".equalsIgnoreCase(inscricao.getTipoOsc()) ||
                    inscricao.getUnidade() == null ||
                    !inscricao.getUnidade().equalsIgnoreCase(admin.getUnidade())) {
                throw new org.springframework.security.access.AccessDeniedException("Acesso restrito à sua própria unidade.");
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
                .oficinaManha(i.getOficinaManha())
                .oficinaTarde(i.getOficinaTarde())
                .dataInscricao(i.getDataInscricao())
                .dataPresenca(i.getDataPresenca())
                .build();
    }
}
