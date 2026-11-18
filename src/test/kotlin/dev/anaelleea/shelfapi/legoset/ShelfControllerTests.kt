package dev.anaelleea.shelfapi.legoset

import dev.anaelleea.shelfapi.ADMIN_UID_PROPERTY
import dev.anaelleea.shelfapi.TestcontainersConfiguration
import dev.anaelleea.shelfapi.asUser
import dev.anaelleea.shelfapi.rebrickable.RebrickableApi
import dev.anaelleea.shelfapi.rebrickable.RebrickableSet
import dev.anaelleea.shelfapi.rebrickable.RebrickableTheme
import dev.anaelleea.shelfapi.user.User
import dev.anaelleea.shelfapi.user.UserRepository
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

/** Covers both a user's own shelf (/api/me/lego-sets) and the public shelves (/api/shelves/{handle}/lego-sets). */
@SpringBootTest(properties = [ADMIN_UID_PROPERTY])
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class ShelfControllerTests @Autowired constructor(
    private val mvc: MockMvcTester,
    private val legoSets: LegoSetRepository,
    private val themes: LegoThemeRepository,
    private val users: UserRepository,
) {

    @MockitoBean
    private lateinit var rebrickable: RebrickableApi

    private val bonsaiImage = "https://cdn.rebrickable.com/media/sets/10281-1/148031.jpg"

    private val alice = "alice-uid"
    private val bob = "bob-uid"

    @BeforeEach
    fun setUp() {
        legoSets.deleteAll()
        themes.deleteAll()
        users.deleteAll()
        users.save(User(alice, "alice"))
        users.save(User(bob, "bob"))
        given(rebrickable.getSet("10281-1"))
            .willReturn(RebrickableSet("10281-1", "Bonsai Tree", 2021, 721, 878, bonsaiImage))
        given(rebrickable.getSet("10311-1"))
            .willReturn(RebrickableSet("10311-1", "Orchid", 2022, 721, 608, null))
        given(rebrickable.getTheme(721)).willReturn(RebrickableTheme(721, "Botanical Collection"))
    }

    @Test
    fun `adds a set by its short number using Rebrickable data`() {
        val result = add(alice, "10281", "QUEUED")

        assertThat(result).hasStatus(HttpStatus.CREATED)
        assertThat(result.response.getHeader("Location")).endsWith("/api/me/lego-sets/10281-1")
        assertThat(result).bodyJson().isLenientlyEqualTo(
            """
            {"setNumber": "10281-1", "name": "Bonsai Tree", "theme": "Botanical Collection", "releaseYear": 2021,
             "pieceCount": 878, "status": "QUEUED", "imageUrl": "$bonsaiImage"}
            """
        )
        assertThat(mvc.get().uri("/api/me/lego-sets/10281-1").with(asUser(alice))).hasStatusOk()
    }

    @Test
    fun `fetches each theme from Rebrickable only once`() {
        add(alice, "10281", "QUEUED")
        add(alice, "10311", "WISHLIST")

        verify(rebrickable, times(1)).getTheme(721)
        assertThat(themes.count()).isEqualTo(1)
    }

    @Test
    fun `rejects a set that is already on the shelf`() {
        add(alice, "10281-1", "QUEUED")

        assertThat(add(alice, "10281", "BUILT")).hasStatus(HttpStatus.CONFLICT)
    }

    @Test
    fun `rejects a set unknown to Rebrickable`() {
        given(rebrickable.getSet("99999-1")).willThrow(
            HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, ByteArray(0), null)
        )

        assertThat(add(alice, "99999", "QUEUED"))
            .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `rejects an invalid request`() {
        assertThat(add(alice, "10281", "LOST")).hasStatus(HttpStatus.BAD_REQUEST)
        assertThat(add(alice, "not a set!", "QUEUED")).hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `lists my sets filtered by status`() {
        add(alice, "10281", "QUEUED")
        add(alice, "10311", "BUILT")

        assertThat(mvc.get().uri("/api/me/lego-sets?status=BUILT").with(asUser(alice)))
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.content[*].setNumber") { assertThat(it).isEqualTo(listOf("10311-1")) }
            .hasPathSatisfying("$.page.totalElements") { assertThat(it).isEqualTo(1) }
    }

    @Test
    fun `updates the status`() {
        add(alice, "10281", "QUEUED")

        assertThat(updateStatus("10281-1", "BUILT").with(asUser(alice)))
            .hasStatusOk().bodyJson().extractingPath("$.status").isEqualTo("BUILT")
    }

    @Test
    fun `deletes a set`() {
        add(alice, "10281", "QUEUED")

        assertThat(mvc.delete().uri("/api/me/lego-sets/10281-1").with(asUser(alice))).hasStatus(HttpStatus.NO_CONTENT)
        assertThat(mvc.get().uri("/api/shelves/alice/lego-sets/10281-1"))
            .hasStatus(HttpStatus.NOT_FOUND)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `keeps each user's shelf separate`() {
        add(alice, "10281", "QUEUED")
        add(alice, "10311", "QUEUED")

        // Bob can add the same set to his own shelf...
        assertThat(add(bob, "10281", "WISHLIST")).hasStatus(HttpStatus.CREATED)
        // ...but can't see or change Alice's copy.
        assertThat(mvc.get().uri("/api/me/lego-sets/10311-1").with(asUser(bob))).hasStatus(HttpStatus.NOT_FOUND)
        assertThat(updateStatus("10311-1", "BUILT").with(asUser(bob))).hasStatus(HttpStatus.NOT_FOUND)
        assertThat(mvc.delete().uri("/api/me/lego-sets/10311-1").with(asUser(bob))).hasStatus(HttpStatus.NOT_FOUND)
        assertThat(mvc.get().uri("/api/shelves/alice/lego-sets/10311-1"))
            .hasStatusOk().bodyJson().extractingPath("$.status").isEqualTo("QUEUED")
        assertThat(mvc.get().uri("/api/me/lego-sets").with(asUser(bob)))
            .hasStatusOk().bodyJson()
            .hasPathSatisfying("$.content[*].status") { assertThat(it).isEqualTo(listOf("WISHLIST")) }
        assertThat(mvc.get().uri("/api/shelves/alice/lego-sets/10281-1"))
            .hasStatusOk().bodyJson().extractingPath("$.status").isEqualTo("QUEUED")
    }

    @Test
    fun `lets guests browse a shelf by its handle`() {
        add(alice, "10281", "WISHLIST")

        assertThat(mvc.get().uri("/api/shelves/alice/lego-sets"))
            .hasStatusOk().bodyJson()
            .hasPathSatisfying("$.content[*].setNumber") { assertThat(it).isEqualTo(listOf("10281-1")) }
        assertThat(mvc.get().uri("/api/shelves/alice/lego-sets/10281-1")).hasStatusOk()
        assertThat(mvc.get().uri("/api/shelves/bob/lego-sets/10281-1")).hasStatus(HttpStatus.NOT_FOUND)
        assertThat(mvc.get().uri("/api/shelves/nobody/lego-sets"))
            .hasStatus(HttpStatus.NOT_FOUND)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `reserves my shelf to signed-in invited users`() {
        add(alice, "10281", "WISHLIST")

        assertThat(addRequest("10311", "QUEUED")).hasStatus(HttpStatus.UNAUTHORIZED)
        assertThat(addRequest("10311", "QUEUED").with(asUser("not-invited"))).hasStatus(HttpStatus.FORBIDDEN)
        assertThat(updateStatus("10281-1", "BUILT")).hasStatus(HttpStatus.UNAUTHORIZED)
        assertThat(updateStatus("10281-1", "BUILT").with(asUser("not-invited"))).hasStatus(HttpStatus.FORBIDDEN)
        assertThat(mvc.delete().uri("/api/me/lego-sets/10281-1")).hasStatus(HttpStatus.UNAUTHORIZED)
        assertThat(mvc.delete().uri("/api/me/lego-sets/10281-1").with(asUser("not-invited"))).hasStatus(HttpStatus.FORBIDDEN)
        assertThat(mvc.get().uri("/api/me/lego-sets")).hasStatus(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `lets a guest gift a wishlisted set`() {
        add(alice, "10281", "WISHLIST")

        assertThat(mvc.post().uri("/api/shelves/alice/lego-sets/10281/gift"))
            .hasStatusOk().bodyJson().extractingPath("$.status").isEqualTo("GIFTED")
    }

    @Test
    fun `only gifts the set on the given shelf`() {
        add(alice, "10281", "WISHLIST")
        add(bob, "10281", "WISHLIST")

        mvc.post().uri("/api/shelves/alice/lego-sets/10281-1/gift").exchange()

        assertThat(mvc.get().uri("/api/shelves/bob/lego-sets/10281-1"))
            .hasStatusOk().bodyJson().extractingPath("$.status").isEqualTo("WISHLIST")
    }

    @Test
    fun `refuses to gift a set twice`() {
        add(alice, "10281", "WISHLIST")
        mvc.post().uri("/api/shelves/alice/lego-sets/10281-1/gift").exchange()

        assertThat(mvc.post().uri("/api/shelves/alice/lego-sets/10281-1/gift"))
            .hasStatus(HttpStatus.CONFLICT)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `refuses to gift a set that is not on the wishlist`() {
        add(alice, "10281", "BUILT")

        assertThat(mvc.post().uri("/api/shelves/alice/lego-sets/10281-1/gift")).hasStatus(HttpStatus.CONFLICT)
        assertThat(mvc.post().uri("/api/shelves/alice/lego-sets/99999-1/gift")).hasStatus(HttpStatus.NOT_FOUND)
        assertThat(mvc.post().uri("/api/shelves/nobody/lego-sets/10281-1/gift")).hasStatus(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `answers CORS preflight requests without a token`() {
        val preflight = mvc.options().uri("/api/me/lego-sets")
            .header(HttpHeaders.ORIGIN, "http://localhost:5173")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type")

        assertThat(preflight).hasStatusOk()
            .headers().hasValue(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173")
    }

    private fun add(firebaseUid: String, setNumber: String, status: String) =
        addRequest(setNumber, status).with(asUser(firebaseUid)).exchange()

    private fun addRequest(setNumber: String, status: String) =
        mvc.post().uri("/api/me/lego-sets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"setNumber": "$setNumber", "status": "$status"}""")

    private fun updateStatus(setNumber: String, status: String) =
        mvc.patch().uri("/api/me/lego-sets/{setNumber}", setNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"status": "$status"}""")

}
