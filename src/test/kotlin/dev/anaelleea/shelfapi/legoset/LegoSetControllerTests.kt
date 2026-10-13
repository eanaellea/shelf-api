package dev.anaelleea.shelfapi.legoset

import dev.anaelleea.shelfapi.TestcontainersConfiguration
import dev.anaelleea.shelfapi.rebrickable.RebrickableApi
import dev.anaelleea.shelfapi.rebrickable.RebrickableSet
import dev.anaelleea.shelfapi.rebrickable.RebrickableTheme
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.web.client.HttpClientErrorException

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class LegoSetControllerTests @Autowired constructor(
    private val mvc: MockMvcTester,
    private val legoSets: LegoSetRepository,
    private val themes: LegoThemeRepository,
) {

    @MockitoBean
    private lateinit var rebrickable: RebrickableApi

    private val bonsaiImage = "https://cdn.rebrickable.com/media/sets/10281-1/148031.jpg"

    @BeforeEach
    fun setUp() {
        legoSets.deleteAll()
        themes.deleteAll()
        given(rebrickable.getSet("10281-1"))
            .willReturn(RebrickableSet("10281-1", "Bonsai Tree", 2021, 721, 878, bonsaiImage))
        given(rebrickable.getSet("10311-1"))
            .willReturn(RebrickableSet("10311-1", "Orchid", 2022, 721, 608, null))
        given(rebrickable.getTheme(721)).willReturn(RebrickableTheme(721, "Botanical Collection"))
    }

    @Test
    fun `adds a set by its short number using Rebrickable data`() {
        val result = add("10281", "QUEUED")

        assertThat(result).hasStatus(HttpStatus.CREATED)
        assertThat(result.response.getHeader("Location")).endsWith("/api/lego-sets/10281-1")
        assertThat(result).bodyJson().isLenientlyEqualTo(
            """
            {"setNumber": "10281-1", "name": "Bonsai Tree", "theme": "Botanical Collection", "releaseYear": 2021,
             "pieceCount": 878, "status": "QUEUED", "imageUrl": "$bonsaiImage"}
            """
        )
    }

    @Test
    fun `fetches each theme from Rebrickable only once`() {
        add("10281", "QUEUED")
        add("10311", "WISHLIST")

        verify(rebrickable, times(1)).getTheme(721)
        assertThat(themes.count()).isEqualTo(1)
    }

    @Test
    fun `rejects a set that is already on the shelf`() {
        add("10281-1", "QUEUED")

        assertThat(add("10281", "BUILT")).hasStatus(HttpStatus.CONFLICT)
    }

    @Test
    fun `rejects a set unknown to Rebrickable`() {
        given(rebrickable.getSet("99999-1")).willThrow(
            HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, ByteArray(0), null)
        )

        assertThat(add("99999", "QUEUED"))
            .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `rejects an invalid request`() {
        assertThat(add("10281", "LOST")).hasStatus(HttpStatus.BAD_REQUEST)
        assertThat(add("not a set!", "QUEUED")).hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `lists sets filtered by status`() {
        add("10281", "QUEUED")
        add("10311", "BUILT")

        assertThat(mvc.get().uri("/api/lego-sets?status=BUILT"))
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.content[*].setNumber") { assertThat(it).isEqualTo(listOf("10311-1")) }
            .hasPathSatisfying("$.page.totalElements") { assertThat(it).isEqualTo(1) }
    }

    @Test
    fun `updates the status`() {
        add("10281", "QUEUED")

        val result = mvc.patch().uri("/api/lego-sets/10281-1")
            .contentType(MediaType.APPLICATION_JSON).content("""{"status": "BUILT"}""")

        assertThat(result).hasStatusOk().bodyJson().extractingPath("$.status").isEqualTo("BUILT")
    }

    @Test
    fun `deletes a set`() {
        add("10281", "QUEUED")

        assertThat(mvc.delete().uri("/api/lego-sets/10281-1")).hasStatus(HttpStatus.NO_CONTENT)
        assertThat(mvc.get().uri("/api/lego-sets/10281-1"))
            .hasStatus(HttpStatus.NOT_FOUND)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    private fun add(setNumber: String, status: String) =
        mvc.post().uri("/api/lego-sets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"setNumber": "$setNumber", "status": "$status"}""")
            .exchange()

}
