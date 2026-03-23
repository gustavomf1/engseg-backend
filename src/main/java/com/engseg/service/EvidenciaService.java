package com.engseg.service;

import com.engseg.entity.Desvio;
import com.engseg.entity.Evidencia;
import com.engseg.entity.NaoConformidade;
import com.engseg.entity.TipoEvidencia;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.EvidenciaRepository;
import com.engseg.repository.NaoConformidadeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;
    private final NaoConformidadeRepository naoConformidadeRepository;
    private final DesvioRepository desvioRepository;
    private final S3StorageService s3StorageService;

    public Evidencia uploadParaNaoConformidade(UUID naoConformidadeId, MultipartFile file, TipoEvidencia tipo) throws IOException {
        NaoConformidade nc = naoConformidadeRepository.findById(naoConformidadeId)
                .orElseThrow(() -> new EntityNotFoundException("Não conformidade não encontrada"));

        String key = s3StorageService.upload(file, "nao-conformidades/" + naoConformidadeId);

        Evidencia evidencia = Evidencia.builder()
                .nomeArquivo(file.getOriginalFilename())
                .urlArquivo(key)
                .dataUpload(LocalDateTime.now())
                .tipoEvidencia(tipo)
                .naoConformidade(nc)
                .build();

        return evidenciaRepository.save(evidencia);
    }

    public List<Evidencia> listarPorNaoConformidade(UUID naoConformidadeId, TipoEvidencia tipo) {
        if (tipo != null) {
            return evidenciaRepository.findByNaoConformidadeIdAndTipoEvidencia(naoConformidadeId, tipo);
        }
        return evidenciaRepository.findByNaoConformidadeId(naoConformidadeId);
    }

    public Evidencia uploadParaDesvio(UUID desvioId, MultipartFile file, TipoEvidencia tipo) throws IOException {
        Desvio desvio = desvioRepository.findById(desvioId)
                .orElseThrow(() -> new EntityNotFoundException("Desvio não encontrado"));

        String key = s3StorageService.upload(file, "desvios/" + desvioId);

        Evidencia evidencia = Evidencia.builder()
                .nomeArquivo(file.getOriginalFilename())
                .urlArquivo(key)
                .dataUpload(LocalDateTime.now())
                .tipoEvidencia(tipo)
                .desvio(desvio)
                .build();

        return evidenciaRepository.save(evidencia);
    }

    public List<Evidencia> listarPorDesvio(UUID desvioId, TipoEvidencia tipo) {
        if (tipo != null) {
            return evidenciaRepository.findByDesvioIdAndTipoEvidencia(desvioId, tipo);
        }
        return evidenciaRepository.findByDesvioId(desvioId);
    }

    public Evidencia buscarPorId(UUID id) {
        return evidenciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evidência não encontrada"));
    }

    public byte[] download(UUID evidenciaId) {
        Evidencia evidencia = buscarPorId(evidenciaId);
        return s3StorageService.download(evidencia.getUrlArquivo());
    }

    public void deletar(UUID evidenciaId) {
        Evidencia evidencia = buscarPorId(evidenciaId);
        s3StorageService.delete(evidencia.getUrlArquivo());
        evidenciaRepository.delete(evidencia);
    }
}
