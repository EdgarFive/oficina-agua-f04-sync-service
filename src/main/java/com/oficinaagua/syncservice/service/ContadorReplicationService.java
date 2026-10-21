
package com.oficinaagua.syncservice.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContadorReplicationService {

    private static final String TABLA = "oficina_agua_contadores";

    private final JdbcTemplate jdbcTemplate;
    private final DynamoDbClient dynamoDbClient;

    public ContadorReplicationService(
            JdbcTemplate jdbcTemplate,
            DynamoDbClient dynamoDbClient) {

        this.jdbcTemplate = jdbcTemplate;
        this.dynamoDbClient = dynamoDbClient;
    }

    public void sincronizarContadores() {

        // 1. Obtener contadores desde MariaDB
        var contadores = jdbcTemplate.query(
            """
            SELECT contador_id, numero_registro,
                   direccion_servicio, estado,
                   cliente_id, tipo_servicio_id
            FROM tb_contadores
            """,
            (rs, fila) -> convertirContador(rs)
        );

        int insertados = 0;
        int actualizados = 0;
        int sinCambios = 0;

        // 2. Comparar cada contador con DynamoDB
        for (var contador : contadores) {

            var clave = Map.of(
                "contador_id",
                contador.get("contador_id")
            );

            var existente = dynamoDbClient.getItem(
                GetItemRequest.builder()
                    .tableName(TABLA)
                    .key(clave)
                    .consistentRead(true)
                    .build()
            ).item();

            // 3. Insertar si todavía no existe
            if (existente == null || existente.isEmpty()) {

                guardarContador(contador);
                insertados++;

            // 4. Actualizar solamente si cambió
            } else if (!existente.equals(contador)) {

                guardarContador(contador);
                actualizados++;

            } else {

                sinCambios++;
            }
        }

        System.out.println("SINCRONIZACION DE CONTADORES");
        System.out.println("Insertados: " + insertados);
        System.out.println("Actualizados: " + actualizados);
        System.out.println("Sin cambios: " + sinCambios);
    }

    private void guardarContador(
            Map<String, AttributeValue> contador) {

        dynamoDbClient.putItem(
            PutItemRequest.builder()
                .tableName(TABLA)
                .item(contador)
                .build()
        );
    }

    private Map<String, AttributeValue> convertirContador(
            ResultSet rs) throws SQLException {

        Map<String, AttributeValue> item = new HashMap<>();

        agregarNumero(item, rs, "contador_id");
        agregarTexto(item, "numero_registro",
                rs.getString("numero_registro"));
        agregarTexto(item, "direccion_servicio",
                rs.getString("direccion_servicio"));
        agregarNumero(item, rs, "estado");
        agregarNumero(item, rs, "cliente_id");
        agregarNumero(item, rs, "tipo_servicio_id");

        return item;
    }

    private void agregarTexto(
            Map<String, AttributeValue> item,
            String campo,
            String valor) {

        if (valor != null) {
            item.put(campo, AttributeValue.fromS(valor));
        }
    }

    private void agregarNumero(
            Map<String, AttributeValue> item,
            ResultSet rs,
            String campo) throws SQLException {

        long valor = rs.getLong(campo);

        if (!rs.wasNull()) {
            item.put(
                campo,
                AttributeValue.fromN(String.valueOf(valor))
            );
        }
    }
}
