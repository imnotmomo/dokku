package dev.coms4156.project.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata describing the restroom finder backend API and default server.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Restroom Finder API",
        description = "Interactive API documentation for the COMS 4156 restroom finder backend"
    ),
    servers = {
        @Server(url = "/", description = "Default server")
    }
)
public class OpenApiConfig {
}
