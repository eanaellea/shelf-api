package dev.anaelleea.shelfapi.user

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(private val users: UserRepository) {

    /** The user signed in with this Firebase UID; 403 when the account hasn't been invited. */
    fun getByFirebaseUid(firebaseUid: String): User =
        users.findByFirebaseUid(firebaseUid) ?: throw UserNotInvitedException()

    /** The owner of a public shelf; 404 when no user has this handle. */
    fun getByHandle(handle: String): User =
        users.findByHandle(handle) ?: throw ShelfNotFoundException(handle)

    @Transactional
    fun create(request: CreateUserRequest): UserResponse {
        if (users.existsByFirebaseUid(request.firebaseUid)) throw UserAlreadyExistsException(request.firebaseUid)
        if (users.existsByHandle(request.handle)) throw HandleTakenException(request.handle)

        return try {
            users.saveAndFlush(User(request.firebaseUid, request.handle)).toResponse()
        } catch (e: DataIntegrityViolationException) {
            throw UserAlreadyExistsException(request.firebaseUid) // invited concurrently, e.g. a double-click
        }
    }

}
