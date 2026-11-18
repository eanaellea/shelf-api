package dev.anaelleea.shelfapi.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByFirebaseUid(firebaseUid: String): User?

    fun findByHandle(handle: String): User?

    fun existsByFirebaseUid(firebaseUid: String): Boolean

    fun existsByHandle(handle: String): Boolean

}
