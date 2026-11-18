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
 * Each user's shelf is a public gift registry: anyone can browse it and mark a wishlisted set as gifted.
 * Everything else needs a Firebase ID token, whose subject is the user's Firebase UID.
 * Signed-in users must also have been invited: UserService answers 403 when they haven't.
 * Inviting users is reserved to the admin.
 */
@Configuration
class SecurityConfig(
    @Value("\${shelf.admin-uid}") private val adminUid: String,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors { } // uses the Spring MVC CORS mappings from WebConfig
            csrf { disable() } // stateless API authenticated by bearer tokens, no cookies
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                authorize(HttpMethod.GET, "/api/shelves/**", permitAll)
                authorize(HttpMethod.POST, "/api/shelves/*/lego-sets/*/gift", permitAll)
                authorize("/api/admin/**", admin)
                authorize("/api/**", authenticated)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/error", permitAll)
                authorize(anyRequest, denyAll)
            }
            oauth2ResourceServer { jwt { } }
        }
        return http.build()
    }

    private val admin = AuthorizationManager<RequestAuthorizationContext> { authentication, _ ->
        AuthorizationDecision(authentication.get()?.name == adminUid)
    }

}
