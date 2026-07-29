package com.example.cooperativevoting.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI cooperativeVotingOpenApi(AppProperties properties) {
    return new OpenAPI()
        .info(
            new Info()
                .title("Cooperative Voting API")
                .version("v1")
                .description(
                    "API REST e contratos de apresentação mobile para votação cooperativa."))
        .servers(
            List.of(
                new Server().url(properties.publicBaseUrl()).description("Ambiente configurado")));
  }
}
