package dev.anaelleea.shelfapi.config

import dev.anaelleea.shelfapi.ADMIN_UID_PROPERTY
import dev.anaelleea.shelfapi.TestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.assertj.MockMvcTester

@SpringBootTest(properties = [ADMIN_UID_PROPERTY])
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class OpenApiTests @Autowired constructor(
    private val mvc: MockMvcTester,
) {

    @Test
    fun `serves the API description without authentication`() {
        assertThat(mvc.get().uri("/v3/api-docs"))
            .hasStatusOk()
            .bodyJson()
            .hasPath("$.paths['/api/me/lego-sets']")
            .hasPath("$.paths['/api/shelves/{handle}/lego-sets']")
            .hasPathSatisfying("$.components.securitySchemes.firebase.scheme") { assertThat(it).isEqualTo("bearer") }
            // signed-in endpoints need a token
            .hasPathSatisfying("$.paths['/api/me/lego-sets'].get.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            .hasPathSatisfying("$.paths['/api/me/lego-sets'].post.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            .hasPathSatisfying("$.paths['/api/me/lego-sets/{setNumber}'].patch.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            .hasPathSatisfying("$.paths['/api/me/lego-sets/{setNumber}'].delete.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            .hasPathSatisfying("$.paths['/api/me'].get.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            .hasPathSatisfying("$.paths['/api/admin/users'].post.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            .hasPathSatisfying("$.paths['/api/catalog/sets'].get.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            // the signed-in user comes from the token, not from a request parameter
            .hasPathSatisfying("$.paths['/api/me/lego-sets'].get.parameters[*].name") { assertThat(it).asArray().doesNotContain("principal") }
            // public endpoints don't need a token
            .doesNotHavePath("$.paths['/api/shelves/{handle}/lego-sets'].get.security")
            .doesNotHavePath("$.paths['/api/shelves/{handle}/lego-sets/{setNumber}'].get.security")
            .doesNotHavePath("$.paths['/api/shelves/{handle}/lego-sets/{setNumber}/gift'].post.security")
    }

    @Test
    fun `serves Swagger UI without authentication`() {
        assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatus(HttpStatus.OK)
    }

}
