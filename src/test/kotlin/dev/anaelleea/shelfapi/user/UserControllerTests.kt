package dev.anaelleea.shelfapi.user

import dev.anaelleea.shelfapi.ADMIN_UID_PROPERTY
import dev.anaelleea.shelfapi.TestcontainersConfiguration
import dev.anaelleea.shelfapi.asAdmin
import dev.anaelleea.shelfapi.asUser
import dev.anaelleea.shelfapi.legoset.LegoSetRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester

@SpringBootTest(properties = [ADMIN_UID_PROPERTY])
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class UserControllerTests @Autowired constructor(
    private val mvc: MockMvcTester,
    private val legoSets: LegoSetRepository,
    private val users: UserRepository,
) {

    @BeforeEach
    fun setUp() {
        legoSets.deleteAll()
        users.deleteAll()
    }

    @Test
    fun `lets the admin invite a user`() {
        assertThat(invite("alice-uid", "alice").with(asAdmin()))
            .hasStatus(HttpStatus.CREATED)
            .bodyJson().isLenientlyEqualTo("""{"firebaseUid": "alice-uid", "handle": "alice"}""")

        assertThat(mvc.get().uri("/api/me").with(asUser("alice-uid")))
            .hasStatusOk().bodyJson().extractingPath("$.handle").isEqualTo("alice")
    }

    @Test
    fun `rejects a Firebase user or handle that already exists`() {
        invite("alice-uid", "alice").with(asAdmin()).exchange()

        assertThat(invite("alice-uid", "other").with(asAdmin()))
            .hasStatus(HttpStatus.CONFLICT)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
        assertThat(invite("other-uid", "alice").with(asAdmin())).hasStatus(HttpStatus.CONFLICT)
    }

    @Test
    fun `rejects an invalid handle`() {
        assertThat(invite("alice-uid", "Alice").with(asAdmin())).hasStatus(HttpStatus.BAD_REQUEST)
        assertThat(invite("alice-uid", "-alice").with(asAdmin())).hasStatus(HttpStatus.BAD_REQUEST)
        assertThat(invite("alice-uid", "a".repeat(31)).with(asAdmin())).hasStatus(HttpStatus.BAD_REQUEST)
        assertThat(invite("", "alice").with(asAdmin())).hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `reserves inviting users to the admin`() {
        users.save(User("alice-uid", "alice"))

        assertThat(invite("bob-uid", "bob")).hasStatus(HttpStatus.UNAUTHORIZED)
        assertThat(invite("bob-uid", "bob").with(asUser("alice-uid"))).hasStatus(HttpStatus.FORBIDDEN)
        assertThat(users.existsByHandle("bob")).isFalse()
    }

    @Test
    fun `refuses Firebase users who have not been invited`() {
        assertThat(mvc.get().uri("/api/me").with(asUser("not-invited")))
            .hasStatus(HttpStatus.FORBIDDEN)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    private fun invite(firebaseUid: String, handle: String) =
        mvc.post().uri("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"firebaseUid": "$firebaseUid", "handle": "$handle"}""")

}
