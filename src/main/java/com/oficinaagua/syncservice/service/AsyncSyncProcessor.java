package com.oficinaagua.syncservice.service;

import com.oficinaagua.syncservice.model.SyncJobResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncSyncProcessor {

    private final SyncJobService syncJobService;

    public AsyncSyncProcessor(SyncJobService syncJobService) {
        this.syncJobService = syncJobService;
    }

    @Async("syncExecutor")
    public void procesar(String jobId) {

        // Indicar que el trabajo comenzó
        syncJobService.actualizarEstado(
                jobId,
                SyncJobResponse.EstadoEnum.EN_PROGRESO
        );

        try {

            // SIMULACIÓN TEMPORAL:
            // Representa una operación que tarda 5 segundos.
            // Todavía NO sincroniza MySQL con DynamoDB.
            Thread.sleep(5000);

            // Finalización de la simulación
            syncJobService.actualizarEstado(
                    jobId,
                    SyncJobResponse.EstadoEnum.COMPLETADO
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            syncJobService.actualizarEstado(
                    jobId,
                    SyncJobResponse.EstadoEnum.FALLIDO
            );

        } catch (Exception e) {

            syncJobService.actualizarEstado(
                    jobId,
                    SyncJobResponse.EstadoEnum.FALLIDO
            );
        }
    }
}
