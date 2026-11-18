package dev.anaelleea.shelfapi

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.RequestPostProcessor

/** Firebase UID of the admin in tests; set with `@SpringBootTest(properties = [ADMIN_UID_PROPERTY])`. */
const val ADMIN_UID = "admin-uid"
const val ADMIN_UID_PROPERTY = "shelf.admin-uid=$ADMIN_UID"

/** Signs the request in as the Firebase user with this UID. */
fun asUser(firebaseUid: String): RequestPostProcessor = jwt().jwt { it.subject(firebaseUid) }

/** Signs the request in as the admin. */
fun asAdmin(): RequestPostProcessor = asUser(ADMIN_UID)
