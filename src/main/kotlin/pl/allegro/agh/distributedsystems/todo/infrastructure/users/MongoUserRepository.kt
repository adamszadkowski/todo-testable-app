package pl.allegro.agh.distributedsystems.todo.infrastructure.users

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import pl.allegro.agh.distributedsystems.todo.domain.users.User
import pl.allegro.agh.distributedsystems.todo.domain.users.UserRepository

class MongoUserRepository(
    private val crud: MongoUserRepositoryCRUD,
) : UserRepository {
    override fun save(user: User) {
        crud.save(user.toEntity())
    }

    override fun findByName(username: String): User? =
        crud.findById(username).orElse(null)
            ?.toDomain()

    private fun User.toEntity() = UserEntity(
        name = username,
        password = password,
        status = status.toString(),
    )

    private fun UserEntity.toDomain() = User(
        username = name,
        password = password,
        status = User.Status.valueOf(status),
    )
}

interface MongoUserRepositoryCRUD : MongoRepository<UserEntity, String>

@Document("users_v1")
data class UserEntity(
    @Id val name: String,
    val password: String,
    val status: String,
)
