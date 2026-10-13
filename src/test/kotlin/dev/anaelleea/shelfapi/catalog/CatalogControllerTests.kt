package dev.anaelleea.shelfapi.catalog

import dev.anaelleea.shelfapi.OWNER_UID_PROPERTY
import dev.anaelleea.shelfapi.TestcontainersConfiguration
import dev.anaelleea.shelfapi.asOwner
import dev.anaelleea.shelfapi.legoset.LegoSet
import dev.anaelleea.shelfapi.legoset.LegoSetRepository
import dev.anaelleea.shelfapi.legoset.LegoSetStatus
import dev.anaelleea.shelfapi.legoset.LegoTheme
import dev.anaelleea.shelfapi.legoset.LegoThemeRepository
import dev.anaelleea.shelfapi.rebrickable.RebrickableApi
import dev.anaelleea.shelfapi.rebrickable.RebrickablePage
import dev.anaelleea.shelfapi.rebrickable.RebrickableSet
import dev.anaelleea.shelfapi.rebrickable.RebrickableTheme
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.web.client.ResourceAccessException

@SpringBootTest(properties = [OWNER_UID_PROPERTY])
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class CatalogControllerTests @Autowired constructor(
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
        given(rebrickable.searchSets("botanical", "-year", 20)).willReturn(
            RebrickablePage(
                listOf(
                    RebrickableSet("10311-1", "Orchid", 2022, 721, 608, null),
                    RebrickableSet("10281-1", "Bonsai Tree", 2021, 721, 878, bonsaiImage),
                )
            )
        )
        given(rebrickable.getThemes(1000)).willReturn(
            RebrickablePage(listOf(RebrickableTheme(721, "Botanical Collection"), RebrickableTheme(158, "Star Wars")))
        )
    }

    @Test
    fun `searches Rebrickable and marks sets already on the shelf`() {
        val theme = themes.save(LegoTheme(721, "Botanical Collection"))
        legoSets.save(LegoSet("10281-1", "Bonsai Tree", theme, 2021, 878, bonsaiImage, LegoSetStatus.BUILT))

        assertThat(search("botanical")).hasStatusOk().bodyJson().isLenientlyEqualTo(
            """
            [
              {"setNumber": "10311-1", "name": "Orchid", "theme": "Botanical Collection", "releaseYear": 2022,
               "pieceCount": 608, "imageUrl": null, "owned": false},
              {"setNumber": "10281-1", "name": "Bonsai Tree", "theme": "Botanical Collection", "releaseYear": 2021,
               "pieceCount": 878, "imageUrl": "$bonsaiImage", "owned": true}
            ]
            """
        )
        verify(rebrickable, never()).getThemes(anyInt())
    }

    @Test
    fun `fetches and caches all themes when one is missing`() {
        search("botanical")
        search("botanical")

        verify(rebrickable).getThemes(1000)
        assertThat(themes.count()).isEqualTo(2)
    }

    @Test
    fun `returns nothing for a blank search`() {
        assertThat(search(" ")).hasStatusOk().bodyJson().isEqualTo("[]")
    }

    @Test
    fun `reports Rebrickable failures as bad gateway`() {
        given(rebrickable.searchSets("botanical", "-year", 20)).willThrow(ResourceAccessException("timeout"))

        assertThat(search("botanical"))
            .hasStatus(HttpStatus.BAD_GATEWAY)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `is reserved to the owner`() {
        assertThat(mvc.get().uri("/api/catalog/sets?search=botanical")).hasStatus(HttpStatus.UNAUTHORIZED)
    }

    private fun search(query: String) =
        mvc.get().uri("/api/catalog/sets?search={query}", query).with(asOwner()).exchange()

}
