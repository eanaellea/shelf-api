package dev.anaelleea.shelfapi.rebrickable

import org.springframework.context.annotation.Configuration
import org.springframework.web.service.registry.ImportHttpServices

// Base URLs and the API key come from spring.http.serviceclient.rebrickable*.* in application.properties.
@Configuration
@ImportHttpServices(group = "rebrickable", types = [RebrickableApi::class])
@ImportHttpServices(group = "rebrickable-cdn", types = [RebrickableCdn::class])
class RebrickableConfiguration
