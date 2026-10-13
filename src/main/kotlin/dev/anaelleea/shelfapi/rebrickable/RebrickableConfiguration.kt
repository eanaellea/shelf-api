package dev.anaelleea.shelfapi.rebrickable

import org.springframework.context.annotation.Configuration
import org.springframework.web.service.registry.ImportHttpServices

// Base URL and API key come from spring.http.serviceclient.rebrickable.* in application.properties.
@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "rebrickable", types = [RebrickableApi::class])
class RebrickableConfiguration
