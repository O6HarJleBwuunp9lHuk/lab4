package org.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userManagementOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User Management API")
                .description("REST API для управления пользователями.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("User Management Team")
                    .email("support@example.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Локальный сервер разработки")
            ));
    }
}
