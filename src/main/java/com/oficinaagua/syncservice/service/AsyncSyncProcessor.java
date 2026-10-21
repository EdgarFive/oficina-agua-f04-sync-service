
package com.oficinaagua.syncservice.service;

import com.oficinaagua.syncservice.model.SyncJobResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncSyncProcessor {

    private final SyncJobService syncJobService;
    private final ContadorReplicationService contadorReplicationService;

    public AsyncSyncProcessor(
            SyncJobService syncJobService,
            ContadorReplicationService contadorReplicationService) {

        this.syncJobService = syncJobService;
        this.contadorReplicationService = contadorReplicationService;
    }

    @Async("syncExecutor")
    public void procesar(String jobId) {

        // Indicar que el trabajo comenzó
        syncJobService.actualizarEstado(
                jobId,
                SyncJobResponse.EstadoEnum.EN_PROGRESO
        );

        try {

            // Ejecutar la replicación REAL de contadores
            contadorReplicationService.sincronizarContadores();

            // Marcar como completado solo si terminó sin errores
            syncJobService.actualizarEstado(
                    jobId,
                    SyncJobResponse.EstadoEnum.COMPLETADO
            );

        } catch (Exception e) {

            System.err.println(
                "ERROR EN SINCRONIZACION - Job: " + jobId
            );

            e.printStackTrace();

            // Registrar que el trabajo falló
            syncJobService.actualizarEstado(
                    jobId,
                    SyncJobResponse.EstadoEnum.FALLIDO
            );
        }
    }
}
