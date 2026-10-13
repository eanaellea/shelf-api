package dev.anaelleea.shelfapi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest(properties = [OWNER_UID_PROPERTY])
@Import(TestcontainersConfiguration::class)
class ShelfApiApplicationTests {

    @Test
    fun contextLoads() {
    }

}
