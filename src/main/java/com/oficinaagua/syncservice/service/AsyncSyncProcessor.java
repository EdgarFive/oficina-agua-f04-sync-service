
package com.oficinaagua.syncservice.service;

import com.oficinaagua.syncservice.model.SyncJobResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncSyncProcessor {

    private final SyncJobService syncJobService;
    private final ContadorReplicationService contadorReplicationService;
    private final LecturaReplicationService lecturaReplicationService;

    public AsyncSyncProcessor(
            SyncJobService syncJobService,
            ContadorReplicationService contadorReplicationService,
            LecturaReplicationService lecturaReplicationService) {

        this.syncJobService = syncJobService;
        this.contadorReplicationService = contadorReplicationService;
        this.lecturaReplicationService = lecturaReplicationService;
    }

    @Async("syncExecutor")
    public void procesar(String jobId) {

        try {

            // Marcar el trabajo como iniciado
            syncJobService.actualizarEstado(
                jobId,
                SyncJobResponse.EstadoEnum.EN_PROGRESO
            );

            // 1. Sincronizar contadores
            contadorReplicationService.sincronizarContadores();

            // 2. Sincronizar lecturas
            lecturaReplicationService.sincronizarLecturas();

            // Marcar como completado cuando ambas
            // sincronizaciones terminen correctamente
            syncJobService.actualizarEstado(
                jobId,
                SyncJobResponse.EstadoEnum.COMPLETADO
            );

        } catch (Exception e) {

            System.err.println(
                "ERROR EN SINCRONIZACION - Job: " + jobId
            );

            e.printStackTrace();

            syncJobService.actualizarEstado(
                jobId,
                SyncJobResponse.EstadoEnum.FALLIDO
            );
        }
    }
}
