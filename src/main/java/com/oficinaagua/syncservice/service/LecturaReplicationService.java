
package com.oficinaagua.syncservice.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LecturaReplicationService {

    private static final String TABLA = "oficina_agua_lecturas";

    private final JdbcTemplate jdbcTemplate;
    private final DynamoDbClient dynamoDbClient;

    public LecturaReplicationService(
            JdbcTemplate jdbcTemplate,
            DynamoDbClient dynamoDbClient) {

        this.jdbcTemplate = jdbcTemplate;
        this.dynamoDbClient = dynamoDbClient;
    }

    public void sincronizarLecturas() {

        // 1. Leer los registros de MariaDB
        var lecturas = jdbcTemplate.query(
            """
            SELECT lectura_id,
                   lectura_anterior,
                   lectura_actual,
                   consumo_litros,
                   litros_exceso,
                   monto_base,
                   monto_exceso,
                   monto_total,
                   fecha,
                   contador_id,
                   usuario_lector_id,
                   tarifa_base_id,
                   tarifa_exceso_id
            FROM tb_lecturas
            """,
            (rs, fila) -> convertirLectura(rs)
        );

        int insertados = 0;
        int actualizados = 0;
        int sinCambios = 0;

        // 2. Comparar cada lectura con DynamoDB
        for (var lectura : lecturas) {

            var clave = Map.of(
                "lectura_id",
                lectura.get("lectura_id")
            );

            var existente = dynamoDbClient.getItem(
                GetItemRequest.builder()
                    .tableName(TABLA)
                    .key(clave)
                    .consistentRead(true)
                    .build()
            ).item();

            // 3. Insertar si no existe
            if (existente == null || existente.isEmpty()) {

                guardarLectura(lectura);
                insertados++;

            // 4. Actualizar si existen diferencias
            } else if (!existente.equals(lectura)) {

                guardarLectura(lectura);
                actualizados++;

            } else {

                sinCambios++;
            }
        }

        System.out.println("SINCRONIZACION DE LECTURAS");
        System.out.println("Insertados: " + insertados);
        System.out.println("Actualizados: " + actualizados);
        System.out.println("Sin cambios: " + sinCambios);
    }

    private void guardarLectura(
            Map<String, AttributeValue> lectura) {

        dynamoDbClient.putItem(
            PutItemRequest.builder()
                .tableName(TABLA)
                .item(lectura)
                .build()
        );
    }

    private Map<String, AttributeValue> convertirLectura(
            ResultSet rs) throws SQLException {

        Map<String, AttributeValue> item = new HashMap<>();

        agregarNumero(item, rs, "lectura_id");
        agregarNumero(item, rs, "lectura_anterior");
        agregarNumero(item, rs, "lectura_actual");
        agregarNumero(item, rs, "consumo_litros");
        agregarNumero(item, rs, "litros_exceso");

        agregarNumero(item, rs, "monto_base");
        agregarNumero(item, rs, "monto_exceso");
        agregarNumero(item, rs, "monto_total");

        agregarTexto(item, "fecha", rs.getString("fecha"));

        agregarNumero(item, rs, "contador_id");
        agregarNumero(item, rs, "usuario_lector_id");
        agregarNumero(item, rs, "tarifa_base_id");
        agregarNumero(item, rs, "tarifa_exceso_id");

        return item;
    }

    private void agregarNumero(
            Map<String, AttributeValue> item,
            ResultSet rs,
            String campo) throws SQLException {

        BigDecimal valor = rs.getBigDecimal(campo);

        if (valor != null) {
            item.put(
                campo,
                AttributeValue.fromN(
                    valor.stripTrailingZeros().toPlainString()
                )
            );
        }
    }

    private void agregarTexto(
            Map<String, AttributeValue> item,
            String campo,
            String valor) {

        if (valor != null) {
            item.put(campo, AttributeValue.fromS(valor));
        }
    }
}
