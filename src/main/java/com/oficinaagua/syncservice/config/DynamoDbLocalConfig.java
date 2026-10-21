
package com.oficinaagua.syncservice.config;

import java.net.URI;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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

    private static final String TABLA = "oficina_agua_contadores";

    @Bean
    public DynamoDbClient dynamoDbClient() {

        return DynamoDbClient.builder()
                .endpointOverride(
                    URI.create("http://localhost:8000")
                )
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

            var tablas = dynamoDbClient.listTables().tableNames();

            if (!tablas.contains(TABLA)) {

                CreateTableRequest solicitud =
                        CreateTableRequest.builder()
                        .tableName(TABLA)
                        .attributeDefinitions(
                            AttributeDefinition.builder()
                                .attributeName("contador_id")
                                .attributeType(ScalarAttributeType.N)
                                .build()
                        )
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("contador_id")
                                .keyType(KeyType.HASH)
                                .build()
                        )
                        .billingMode(BillingMode.PAY_PER_REQUEST)
                        .build();

                dynamoDbClient.createTable(solicitud);

                System.out.println(
                    "DYNAMODB - Tabla creada: " + TABLA
                );

            } else {

                System.out.println(
                    "DYNAMODB - La tabla ya existe: " + TABLA
                );
            }

            System.out.println(
                "DYNAMODB LOCAL OK - Tablas disponibles: "
                + dynamoDbClient.listTables().tableNames()
            );
        };
    }
}
