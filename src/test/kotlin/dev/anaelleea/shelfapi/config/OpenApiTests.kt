package dev.anaelleea.shelfapi.config

import dev.anaelleea.shelfapi.OWNER_UID_PROPERTY
import dev.anaelleea.shelfapi.TestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.assertj.MockMvcTester

@SpringBootTest(properties = [OWNER_UID_PROPERTY])
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
            .hasPath("$.paths['/api/lego-sets']")
            .hasPathSatisfying("$.components.securitySchemes.firebase.scheme") { assertThat(it).isEqualTo("bearer") }
            // owner endpoints need a token
            .hasPathSatisfying("$.paths['/api/lego-sets'].post.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            .hasPathSatisfying("$.paths['/api/lego-sets/{setNumber}'].patch.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            .hasPathSatisfying("$.paths['/api/lego-sets/{setNumber}'].delete.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            .hasPathSatisfying("$.paths['/api/catalog/sets'].get.security[0]") { assertThat(it).asMap().containsKey("firebase") }
            // public endpoints don't
            .doesNotHavePath("$.paths['/api/lego-sets'].get.security")
            .doesNotHavePath("$.paths['/api/lego-sets/{setNumber}'].get.security")
            .doesNotHavePath("$.paths['/api/lego-sets/{setNumber}/gift'].post.security")
    }

    @Test
    fun `serves Swagger UI without authentication`() {
        assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatus(HttpStatus.OK)
    }

}
