
package com.oficinaagua.syncservice.controller;

import com.oficinaagua.syncservice.api.ApiApi;
import com.oficinaagua.syncservice.model.SyncJobResponse;
import com.oficinaagua.syncservice.service.SyncJobService;
import com.oficinaagua.syncservice.service.AsyncSyncProcessor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SyncController implements ApiApi {

    private final SyncJobService syncJobService;
    private final AsyncSyncProcessor asyncSyncProcessor;

    // Inyectar los servicios necesarios
    public SyncController(
            SyncJobService syncJobService,
            AsyncSyncProcessor asyncSyncProcessor) {

        this.syncJobService = syncJobService;
        this.asyncSyncProcessor = asyncSyncProcessor;
    }

    @Override
    public ResponseEntity<SyncJobResponse> iniciarSincronizacion() {

        // 1. Crear un nuevo job con estado PENDIENTE
        SyncJobResponse job = syncJobService.crearJob();

        // 2. Iniciar el procesamiento en segundo plano
        asyncSyncProcessor.procesar(job.getJobId());

        // 3. Responder inmediatamente con HTTP 202
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(job);
    }

    @Override
    public ResponseEntity<SyncJobResponse> consultarEstadoJob(
            String jobId) {

        // Consultar el estado actual del job
        SyncJobResponse job = syncJobService.consultarJob(jobId);

        // Si no existe, devolver HTTP 404
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        // Devolver el estado del job
        return ResponseEntity.ok(job);
    }
}
