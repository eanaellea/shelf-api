package dev.anaelleea.shelfapi

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.RequestPostProcessor

/** Firebase UID of the shelf owner in tests; set with `@SpringBootTest(properties = [OWNER_UID_PROPERTY])`. */
const val OWNER_UID = "owner-uid"
const val OWNER_UID_PROPERTY = "shelf.owner-uid=$OWNER_UID"

/** Signs the request in as the shelf owner. */
fun asOwner(): RequestPostProcessor = jwt().jwt { it.subject(OWNER_UID) }

/** Signs the request in as a Firebase user who isn't the owner. */
fun asOtherUser(): RequestPostProcessor = jwt().jwt { it.subject("someone-else") }
