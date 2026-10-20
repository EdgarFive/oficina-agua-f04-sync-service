
package com.oficinaagua.syncservice.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("local")
public class LocalDatabaseCheck {

    @Bean
    public ApplicationRunner verificarMariaDB(
            JdbcTemplate jdbcTemplate) {

        return args -> {

            Integer totalContadores =
                    jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM tb_contadores",
                        Integer.class
                    );

            System.out.println(
                "MARIA DB OK - Contadores encontrados: "
                + totalContadores
            );
        };
    }
}
