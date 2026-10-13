package dev.anaelleea.shelfapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.RequestAuthorizationContext

/**
 * The shelf is a public gift registry: anyone can browse it and mark a wishlisted set as gifted.
 * Everything else is reserved to the owner, identified by the Firebase ID token's subject (the user's UID).
 */
@Configuration(proxyBeanMethods = false)
class SecurityConfig(
    @Value("\${shelf.owner-uid}") private val ownerUid: String,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors { } // uses the Spring MVC CORS mappings from WebConfig
            csrf { disable() } // stateless API authenticated by bearer tokens, no cookies
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                authorize(HttpMethod.GET, "/api/lego-sets/**", permitAll)
                authorize(HttpMethod.POST, "/api/lego-sets/*/gift", permitAll)
                authorize("/api/**", owner)
                authorize("/error", permitAll)
                authorize(anyRequest, denyAll)
            }
            oauth2ResourceServer { jwt { } }
        }
        return http.build()
    }

    private val owner = AuthorizationManager<RequestAuthorizationContext> { authentication, _ ->
        AuthorizationDecision(authentication.get()?.name == ownerUid)
    }

}
