package com.engseg.service;

import com.engseg.entity.*;
import com.engseg.exception.BusinessException;
import com.engseg.repository.AtividadePlanoAcaoRepository;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.EvidenciaRepository;
import com.engseg.repository.ExecucaoSnapshotRepository;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UsuarioRepository usuarioRepository;
    private final S3StorageService s3StorageService;
    private final AtividadePlanoAcaoRepository atividadePlanoAcaoRepository;
    private final ExecucaoSnapshotRepository execucaoSnapshotRepository;

    private boolean isTecnico() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);
        return usuario != null && usuario.getPerfil() == PerfilUsuario.TECNICO;
    }

    public Evidencia uploadParaNaoConformidade(UUID naoConformidadeId, MultipartFile file, TipoEvidencia tipo) throws IOException {
        NaoConformidade nc = naoConformidadeRepository.findById(naoConformidadeId)
                .orElseThrow(() -> new EntityNotFoundException("Não conformidade não encontrada"));

        if (isTecnico() && nc.getStatus() != StatusNaoConformidade.ABERTA) {
            throw new BusinessException("Técnico não pode adicionar evidências em NC que não está com status ABERTA");
        }

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

        if (isTecnico() && desvio.getStatus() == StatusDesvio.CONCLUIDO) {
            throw new BusinessException("Técnico não pode adicionar evidências em desvio concluído");
        }

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

    public Evidencia uploadParaAtividade(UUID atividadeId, MultipartFile file, TipoEvidencia tipo) throws IOException {
        AtividadePlanoAcao atividade = atividadePlanoAcaoRepository.findById(atividadeId)
                .orElseThrow(() -> new EntityNotFoundException("Atividade não encontrada"));

        String key = s3StorageService.upload(file, "atividades/" + atividadeId);

        Evidencia evidencia = Evidencia.builder()
                .nomeArquivo(file.getOriginalFilename())
                .urlArquivo(key)
                .dataUpload(LocalDateTime.now())
                .tipoEvidencia(tipo)
                .atividadePlanoAcao(atividade)
                .build();

        return evidenciaRepository.save(evidencia);
    }

    public List<Evidencia> listarPorAtividade(UUID atividadeId) {
        return evidenciaRepository.findByAtividadePlanoAcaoId(atividadeId);
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

        if (isTecnico()) {
            if (evidencia.getDesvio() != null) {
                throw new BusinessException("Técnico não pode excluir evidências de desvio concluído");
            }
            if (evidencia.getNaoConformidade() != null && evidencia.getNaoConformidade().getStatus() != StatusNaoConformidade.ABERTA) {
                throw new BusinessException("Técnico não pode excluir evidências de NC que não está com status ABERTA");
            }
        }

        // Remove referência da tabela junction execucao_snapshot_evidencia antes de deletar
        List<ExecucaoSnapshot> snapshots = execucaoSnapshotRepository.findByEvidenciasId(evidencia.getId());
        for (ExecucaoSnapshot snapshot : snapshots) {
            snapshot.getEvidencias().remove(evidencia);
            execucaoSnapshotRepository.save(snapshot);
        }

        s3StorageService.delete(evidencia.getUrlArquivo());
        evidenciaRepository.delete(evidencia);
    }
}
