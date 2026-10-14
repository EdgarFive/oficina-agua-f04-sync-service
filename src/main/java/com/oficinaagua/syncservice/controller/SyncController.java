package com.oficinaagua.syncservice.controller;

import com.oficinaagua.syncservice.api.ApiApi;
import com.oficinaagua.syncservice.model.SyncJobResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SyncController implements ApiApi {

    private final Map<String, SyncJobResponse> jobs =
            new ConcurrentHashMap<>();

    @Override
    public ResponseEntity<SyncJobResponse> iniciarSincronizacion() {

        // Generar un identificador único para el trabajo
        String jobId = UUID.randomUUID().toString();

        // Crear el trabajo con estado inicial PENDIENTE
        SyncJobResponse job = new SyncJobResponse();

        job.setJobId(jobId);
        job.setEstado(SyncJobResponse.EstadoEnum.PENDIENTE);

        // Guardar temporalmente el trabajo en memoria
        jobs.put(jobId, job);

        // Responder HTTP 202 Accepted
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(job);
    }

    @Override
    public ResponseEntity<SyncJobResponse> consultarEstadoJob(
            String jobId) {

        // Buscar el trabajo por su identificador
        SyncJobResponse job = jobs.get(jobId);

        // Si no existe, responder HTTP 404
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        // Devolver el estado actual del trabajo
        return ResponseEntity.ok(job);
    }
}
