package dev.anaelleea.shelfapi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Serves the API description at /v3/api-docs and Swagger UI at /swagger-ui.html.
 * Owner-only endpoints take a Firebase ID token, which can be pasted into Swagger UI's "Authorize" dialog.
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun openApi(): OpenAPI {
        val firebaseToken = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("Firebase ID token of the shelf owner")

        return OpenAPI()
            .info(Info().title("Shelf API").version("v1"))
            .components(Components().addSecuritySchemes("firebase", firebaseToken))
    }

}
