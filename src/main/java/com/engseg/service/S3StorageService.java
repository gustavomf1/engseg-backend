package com.engseg.service;

import com.engseg.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf"
    );

    @Value("${s3.bucket}")
    private String bucket;

    @PostConstruct
    public void initBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket '{}' já existe", bucket);
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket '{}' criado", bucket);
        }
    }

    public String upload(MultipartFile file, String pasta) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo vazio.");
        }

        byte[] bytes = file.getBytes();

        // Valida o TIPO REAL pelos magic bytes — não confia no Content-Type do cliente (M3)
        String detected = detectContentType(bytes);
        if (detected == null || !ALLOWED_CONTENT_TYPES.contains(detected)) {
            throw new BusinessException("Tipo de arquivo não permitido. Envie imagens (JPEG, PNG, GIF, WebP) ou PDF.");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "arquivo";
        // Remove path separators e caracteres especiais para evitar path traversal
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        String key = pasta + "/" + UUID.randomUUID() + "_" + safeName;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(detected) // usa o tipo detectado, não o informado pelo cliente
                        .build(),
                RequestBody.fromBytes(bytes)
        );

        log.info("Arquivo '{}' ({}) enviado para o bucket '{}'", key, detected, bucket);
        return key;
    }

    /** Detecta o MIME real pelos primeiros bytes (file signature). Retorna null se desconhecido. */
    private String detectContentType(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A && (b[6] & 0xFF) == 0x1A && (b[7] & 0xFF) == 0x0A) {
            return "image/png";
        }
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a') {
            return "image/gif";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        if (b.length >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-') {
            return "application/pdf";
        }
        return null;
    }

    public byte[] download(String key) {
        return s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        ).asByteArray();
    }

    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
        log.info("Arquivo '{}' removido do bucket '{}'", key, bucket);
    }
}
