package com.oficinaagua.syncservice.service;

import com.oficinaagua.syncservice.model.SyncJobResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SyncJobService {

    private final Map<String, SyncJobResponse.EstadoEnum> jobs =
            new ConcurrentHashMap<>();

    // Crear un nuevo trabajo
    public SyncJobResponse crearJob() {

        String jobId = UUID.randomUUID().toString();

        jobs.put(jobId, SyncJobResponse.EstadoEnum.PENDIENTE);

        return construirRespuesta(
                jobId,
                SyncJobResponse.EstadoEnum.PENDIENTE
        );
    }

    // Consultar el estado de un trabajo
    public SyncJobResponse consultarJob(String jobId) {

        SyncJobResponse.EstadoEnum estado = jobs.get(jobId);

        if (estado == null) {
            return null;
        }

        return construirRespuesta(jobId, estado);
    }

    // Actualizar el estado de un trabajo existente
    public void actualizarEstado(
            String jobId,
            SyncJobResponse.EstadoEnum nuevoEstado) {

        jobs.computeIfPresent(
                jobId,
                (id, estadoAnterior) -> nuevoEstado
        );
    }

    // Construir la respuesta de la API
    private SyncJobResponse construirRespuesta(
            String jobId,
            SyncJobResponse.EstadoEnum estado) {

        SyncJobResponse respuesta = new SyncJobResponse();

        respuesta.setJobId(jobId);
        respuesta.setEstado(estado);

        return respuesta;
    }
}
