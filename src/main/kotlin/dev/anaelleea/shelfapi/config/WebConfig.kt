package dev.anaelleea.shelfapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration(proxyBeanMethods = false)
class WebConfig(
    @Value("\${shelf.cors.allowed-origins}") private val allowedOrigins: Array<String>,
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(*allowedOrigins)
            .allowedMethods("GET", "POST", "PATCH", "DELETE")
            .exposedHeaders("Location")
    }

}
