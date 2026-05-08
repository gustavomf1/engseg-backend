package com.engseg.service;

import com.engseg.dto.request.SyncBatchRequest;
import com.engseg.dto.request.SyncItemRequest;
import com.engseg.dto.response.SyncBatchResponse;
import com.engseg.dto.response.SyncItemResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final NaoConformidadeService ncService;
    private final DesvioService desvioService;

    public SyncBatchResponse processar(SyncBatchRequest batch) {
        List<SyncItemResult> results = new ArrayList<>();
        for (SyncItemRequest item : batch.items()) {
            results.add(processarItem(item));
        }
        return new SyncBatchResponse(results);
    }

    private SyncItemResult processarItem(SyncItemRequest item) {
        try {
            UUID serverId = switch (item.tipo()) {
                case "NC" -> ncService.create(item.nc()).id();
                case "DESVIO" -> desvioService.create(item.desvio()).id();
                default -> throw new IllegalArgumentException("tipo desconhecido: " + item.tipo());
            };
            return new SyncItemResult(item.localId(), serverId, "CRIADO", null);
        } catch (Exception e) {
            log.warn("SyncService: erro ao processar localId={} tipo={}: {}",
                    item.localId(), item.tipo(), e.getMessage());
            return new SyncItemResult(item.localId(), null, "ERRO", e.getMessage());
        }
    }
}
