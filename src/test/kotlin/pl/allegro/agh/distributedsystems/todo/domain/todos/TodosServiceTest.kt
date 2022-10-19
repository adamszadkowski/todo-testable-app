package pl.allegro.agh.distributedsystems.todo.domain.todos

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pl.allegro.agh.distributedsystems.todo.domain.users.User
import pl.allegro.agh.distributedsystems.todo.domain.users.UserRepository
import pl.allegro.agh.distributedsystems.todo.infrastructure.todos.InMemoryTodosRepository
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.message

class TodosServiceTest {
    private val repository = InMemoryTodosRepository()
    private val userRepository = StubUserRepository()
    private val service = TodosService(repository, userRepository)

    @AfterEach
    fun tearDown() {
        repository.clear()
    }

    @Nested
    inner class `active user` {
        private val activeUser = "user"

        @BeforeEach
        fun `set active user`() {
            userRepository.users[activeUser] = User(
                username = activeUser,
                status = User.Status.ACTIVE,
            )
        }

        @Test
        fun `generate unique ids on save`() {
            val uniqueIds = (1..100)
                .map { service.save(activeUser, "todo name $it") }
                .map { it.id }
                .toSet()

            expectThat(uniqueIds).hasSize(100)
        }
    }

    @Nested
    inner class `user blocking` {

        @Test
        fun `reject saves by missing user`() {
            expectThrows<TodosService.CannotSaveException> {
                service.save("user", "todo name")
            }.message.isEqualTo("User is not active")
        }

        @Test
        fun `reject saves by blocked user`() {
            userRepository.users["user"] = User(
                username = "user",
                status = User.Status.BLOCKED,
            )

            expectThrows<TodosService.CannotSaveException> {
                service.save("user", "todo name")
            }.message.isEqualTo("User is not active")
        }
    }
}

class StubUserRepository : UserRepository {
    val users: MutableMap<String, User> = mutableMapOf()

    override fun findByName(username: String): User? = users[username]
}
