package dev.anaelleea.shelfapi.image

import dev.anaelleea.shelfapi.ADMIN_UID_PROPERTY
import dev.anaelleea.shelfapi.TestcontainersConfiguration
import dev.anaelleea.shelfapi.rebrickable.RebrickableCdn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException

@SpringBootTest(properties = [ADMIN_UID_PROPERTY])
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class ImageControllerTests @Autowired constructor(
    private val mvc: MockMvcTester,
) {

    @MockitoBean
    private lateinit var cdn: RebrickableCdn

    private val bonsaiImage = "/api/images/rebrickable/sets/10281-1/148031.jpg"
    private val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

    @Test
    fun `serves a Rebrickable set image to guests, cached for a long time`() {
        given(cdn.getSetImage("10281-1", "148031.jpg"))
            .willReturn(ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(jpeg))

        assertThat(mvc.get().uri(bonsaiImage)).hasStatusOk()
            .hasContentType(MediaType.IMAGE_JPEG)
            .hasHeader(HttpHeaders.CACHE_CONTROL, "max-age=2592000, public, immutable")
            .body().isEqualTo(jpeg)
    }

    @Test
    fun `serves set images named after the set`() {
        given(cdn.getFlatSetImage("8880-1.jpg"))
            .willReturn(ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(jpeg))

        assertThat(mvc.get().uri("/api/images/rebrickable/sets/8880-1.jpg")).hasStatusOk()
            .hasContentType(MediaType.IMAGE_JPEG)
            .body().isEqualTo(jpeg)
    }

    @Test
    fun `serves small versions of set images`() {
        given(cdn.getSetThumbnail("10281-1", "148031.jpg"))
            .willReturn(ResponseEntity.ok().contentType(MediaType.parseMediaType("image/jpeg; charset=utf-8")).body(jpeg))
        given(cdn.getFlatSetThumbnail("8880-1.jpg"))
            .willReturn(ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(jpeg))

        assertThat(mvc.get().uri("/api/images/rebrickable/thumbs/sets/10281-1/148031.jpg")).hasStatusOk()
            .hasHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
            .body().isEqualTo(jpeg)
        assertThat(mvc.get().uri("/api/images/rebrickable/thumbs/sets/8880-1.jpg")).hasStatusOk()
            .body().isEqualTo(jpeg)
    }

    @Test
    fun `lets the web app load images from its origin`() {
        given(cdn.getSetImage("10281-1", "148031.jpg"))
            .willReturn(ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(jpeg))

        assertThat(mvc.get().uri(bonsaiImage).header(HttpHeaders.ORIGIN, "http://localhost:5173")).hasStatusOk()
            .headers().hasValue(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173")
    }

    @Test
    fun `only fetches paths shaped like set images`() {
        assertThat(mvc.get().uri("/api/images/rebrickable/sets/10281-1/notes.txt")).hasStatus(HttpStatus.NOT_FOUND)
        assertThat(mvc.get().uri("/api/images/rebrickable/parts/10281-1/148031.jpg")).hasStatus(HttpStatus.NOT_FOUND)
        verifyNoInteractions(cdn)
    }

    @Test
    fun `reports images missing from Rebrickable as not found`() {
        given(cdn.getSetImage("10281-1", "148031.jpg"))
            .willThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders(), null, null))

        assertThat(mvc.get().uri(bonsaiImage))
            .hasStatus(HttpStatus.NOT_FOUND)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `reports Rebrickable failures as bad gateway`() {
        given(cdn.getSetImage("10281-1", "148031.jpg")).willThrow(ResourceAccessException("timeout"))

        assertThat(mvc.get().uri(bonsaiImage)).hasStatus(HttpStatus.BAD_GATEWAY)
    }

    @Test
    fun `refuses to pass on anything but an image`() {
        given(cdn.getSetImage("10281-1", "148031.jpg"))
            .willReturn(ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body("<html>".toByteArray()))

        assertThat(mvc.get().uri(bonsaiImage)).hasStatus(HttpStatus.BAD_GATEWAY)
    }

}
