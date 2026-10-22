
package com.oficinaagua.syncservice.config;

import java.net.URI;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Configuration
@Profile("local")
public class DynamoDbLocalConfig {

    @Bean
    public DynamoDbClient dynamoDbClient(@Value("${F04_DYNAMODB_ENDPOINT:http://localhost:8000}") String endpoint) {

        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("local", "local")
                    )
                )
                .build();
    }

    @Bean
    public ApplicationRunner verificarDynamoDB(
            DynamoDbClient dynamoDbClient) {

        return args -> {

            crearTablaSiNoExiste(
                dynamoDbClient,
                "oficina_agua_contadores",
                "contador_id",
                ScalarAttributeType.N
            );

            crearTablaSiNoExiste(
                dynamoDbClient,
                "oficina_agua_sync_jobs",
                "jobId",
                ScalarAttributeType.S
            );

            crearTablaSiNoExiste(
                dynamoDbClient,
                "oficina_agua_lecturas",
                "lectura_id",
                ScalarAttributeType.N
            );

            System.out.println(
                "DYNAMODB LOCAL OK - Tablas disponibles: "
                + dynamoDbClient.listTables().tableNames()
            );
        };
    }


    private void crearTablaSiNoExiste(
            DynamoDbClient dynamoDbClient,
            String nombreTabla,
            String clavePrimaria,
            ScalarAttributeType tipoClave) {

        var tablas = dynamoDbClient.listTables().tableNames();

        if (tablas.contains(nombreTabla)) {

            System.out.println(
                "DYNAMODB - La tabla ya existe: " + nombreTabla
            );

            return;
        }

        CreateTableRequest solicitud =
                CreateTableRequest.builder()
                    .tableName(nombreTabla)
                    .attributeDefinitions(
                        AttributeDefinition.builder()
                            .attributeName(clavePrimaria)
                            .attributeType(tipoClave)
                            .build()
                    )
                    .keySchema(
                        KeySchemaElement.builder()
                            .attributeName(clavePrimaria)
                            .keyType(KeyType.HASH)
                            .build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

        dynamoDbClient.createTable(solicitud);

        System.out.println(
            "DYNAMODB - Tabla creada: " + nombreTabla
        );
    }
}
