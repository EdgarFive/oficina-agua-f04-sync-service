package com.oficinaagua.syncservice;

import com.oficinaagua.syncservice.model.SyncJobResponse;
import com.oficinaagua.syncservice.service.AsyncSyncProcessor;
import com.oficinaagua.syncservice.service.ContadorReplicationService;
import com.oficinaagua.syncservice.service.LecturaReplicationService;
import com.oficinaagua.syncservice.service.SyncJobService;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AsyncSyncProcessorTest {

    @Test
    void debeMarcarFallidoCuandoOcurreUnError() {

        // Simular los servicios sin conectarse a las bases
        SyncJobService jobService = mock(SyncJobService.class);

        ContadorReplicationService contadorService =
                mock(ContadorReplicationService.class);

        LecturaReplicationService lecturaService =
                mock(LecturaReplicationService.class);

        String jobId = "job-simulado";

        // Simular un error de conexión a MariaDB
        doThrow(new RuntimeException("Error simulado de MariaDB"))
                .when(contadorService)
                .sincronizarContadores();

        AsyncSyncProcessor procesador = new AsyncSyncProcessor(
                jobService,
                contadorService,
                lecturaService
        );

        // Ejecutar el procesamiento
        procesador.procesar(jobId);

        // Verificar las transiciones de estado
        var orden = inOrder(jobService);

        orden.verify(jobService).actualizarEstado(
                jobId,
                SyncJobResponse.EstadoEnum.EN_PROGRESO
        );

        orden.verify(jobService).actualizarEstado(
                jobId,
                SyncJobResponse.EstadoEnum.FALLIDO
        );

        // Nunca debe marcarse como completado
        verify(jobService, never()).actualizarEstado(
                jobId,
                SyncJobResponse.EstadoEnum.COMPLETADO
        );

        // No debe continuar a las lecturas si fallaron los contadores
        verifyNoInteractions(lecturaService);
    }
}
