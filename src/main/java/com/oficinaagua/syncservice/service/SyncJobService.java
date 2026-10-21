
package com.oficinaagua.syncservice.service;

import com.oficinaagua.syncservice.model.SyncJobResponse;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.Map;
import java.util.UUID;

@Service
public class SyncJobService {

    private static final String TABLA = "oficina_agua_sync_jobs";

    private final DynamoDbClient dynamoDbClient;

    public SyncJobService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    // Crear un job y guardarlo en DynamoDB
    public SyncJobResponse crearJob() {

        String jobId = UUID.randomUUID().toString();

        dynamoDbClient.putItem(
            PutItemRequest.builder()
                .tableName(TABLA)
                .item(Map.of(
                    "jobId", AttributeValue.fromS(jobId),
                    "estado", AttributeValue.fromS("PENDIENTE")
                ))
                .build()
        );

        return construirRespuesta(
            jobId,
            SyncJobResponse.EstadoEnum.PENDIENTE
        );
    }

    // Consultar un job guardado en DynamoDB
    public SyncJobResponse consultarJob(String jobId) {

        var respuesta = dynamoDbClient.getItem(
            GetItemRequest.builder()
                .tableName(TABLA)
                .key(Map.of(
                    "jobId", AttributeValue.fromS(jobId)
                ))
                .consistentRead(true)
                .build()
        );

        if (!respuesta.hasItem()) {
            return null;
        }

        String estado = respuesta.item()
                .get("estado")
                .s();

        return construirRespuesta(
            jobId,
            SyncJobResponse.EstadoEnum.fromValue(estado)
        );
    }

    // Actualizar el estado de un job existente
    public void actualizarEstado(
            String jobId,
            SyncJobResponse.EstadoEnum nuevoEstado) {

        dynamoDbClient.updateItem(
            UpdateItemRequest.builder()
                .tableName(TABLA)
                .key(Map.of(
                    "jobId", AttributeValue.fromS(jobId)
                ))
                .updateExpression("SET #estado = :estado")
                .conditionExpression("attribute_exists(jobId)")
                .expressionAttributeNames(Map.of(
                    "#estado", "estado"
                ))
                .expressionAttributeValues(Map.of(
                    ":estado", AttributeValue.fromS(
                        nuevoEstado.getValue()
                    )
                ))
                .build()
        );
    }

    private SyncJobResponse construirRespuesta(
            String jobId,
            SyncJobResponse.EstadoEnum estado) {

        SyncJobResponse respuesta = new SyncJobResponse();
        respuesta.setJobId(jobId);
        respuesta.setEstado(estado);

        return respuesta;
    }
}
