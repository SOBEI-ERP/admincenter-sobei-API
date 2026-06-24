package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.Candidatura;
import br.org.sobei.denuncias.model.entity.Vaga;
import br.org.sobei.denuncias.model.enums.StatusVaga;
import br.org.sobei.denuncias.repository.CandidaturaRepository;
import br.org.sobei.denuncias.repository.VagaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidaturaServiceTest {

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private VagaRepository vagaRepository;

    @InjectMocks
    private CandidaturaService candidaturaService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(candidaturaService, "uploadDir", "target/test-uploads");
    }

    @Test
    void testCandidatarVagaFechadaThrowsException() {
        Vaga vaga = Vaga.builder()
                .id(1)
                .status(StatusVaga.FECHADA) // Fechada
                .build();

        MultipartFile file = mock(MultipartFile.class);

        when(vagaRepository.findById(1)).thenReturn(Optional.of(vaga));

        assertThrows(IllegalArgumentException.class, () -> {
            candidaturaService.candidatar(1, "João", "joao@email.com", "11999999999", "Apresentacao", file);
        });

        verify(candidaturaRepository, never()).save(any(Candidatura.class));
    }

    @Test
    void testCandidatarCurriculoVazioThrowsException() {
        Vaga vaga = Vaga.builder()
                .id(1)
                .status(StatusVaga.ABERTA)
                .build();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        when(vagaRepository.findById(1)).thenReturn(Optional.of(vaga));

        assertThrows(IllegalArgumentException.class, () -> {
            candidaturaService.candidatar(1, "João", "joao@email.com", "11999999999", "Apresentacao", file);
        });

        verify(candidaturaRepository, never()).save(any(Candidatura.class));
    }

    @Test
    void testCandidatarCurriculoFormatoInvalidoThrowsException() {
        Vaga vaga = Vaga.builder()
                .id(1)
                .status(StatusVaga.ABERTA)
                .build();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1000L);
        when(file.getContentType()).thenReturn("image/png"); // Formato inválido

        when(vagaRepository.findById(1)).thenReturn(Optional.of(vaga));

        assertThrows(IllegalArgumentException.class, () -> {
            candidaturaService.candidatar(1, "João", "joao@email.com", "11999999999", "Apresentacao", file);
        });

        verify(candidaturaRepository, never()).save(any(Candidatura.class));
    }

    @Test
    void testCandidatarCurriculoTamanhoExcedidoThrowsException() {
        Vaga vaga = Vaga.builder()
                .id(1)
                .status(StatusVaga.ABERTA)
                .build();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(6 * 1024 * 1024L); // 6MB, excede o limite de 5MB

        when(vagaRepository.findById(1)).thenReturn(Optional.of(vaga));

        assertThrows(IllegalArgumentException.class, () -> {
            candidaturaService.candidatar(1, "João", "joao@email.com", "11999999999", "Apresentacao", file);
        });

        verify(candidaturaRepository, never()).save(any(Candidatura.class));
    }

    @Test
    void testCandidatarComSucesso() throws IOException {
        Vaga vaga = Vaga.builder()
                .id(1)
                .status(StatusVaga.ABERTA)
                .unidade("Imbuias")
                .build();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1000L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("curriculo.pdf");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("conteudo".getBytes()));

        when(vagaRepository.findById(1)).thenReturn(Optional.of(vaga));
        when(candidaturaRepository.save(any(Candidatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> {
            candidaturaService.candidatar(1, "João Silva", "joao@email.com", "11999999999", "Apresentacao", file);
        });

        verify(candidaturaRepository, times(1)).save(any(Candidatura.class));
    }
}
