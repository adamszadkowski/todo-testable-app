package pl.allegro.agh.distributedsystems.todo.domain.todos

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pl.allegro.agh.distributedsystems.todo.domain.users.User
import pl.allegro.agh.distributedsystems.todo.domain.users.UserRepository
import pl.allegro.agh.distributedsystems.todo.infrastructure.todos.InMemoryTodosRepository
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.*

class TodosServiceTest {
    private val repository = InMemoryTodosRepository()
    private val userRepository = MockUserRepository()
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
            userRepository.users.computeIfAbsent(activeUser) { mutableListOf() } += User(
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
            userRepository.users.computeIfAbsent("user") { mutableListOf() } += User(
                username = "user",
                status = User.Status.BLOCKED,
            )

            expectThrows<TodosService.CannotSaveException> {
                service.save("user", "todo name")
            }.message.isEqualTo("User is not active")
            expectThat(userRepository.calls).hasEntry("user", 1)
        }

        @Test
        fun `check user status twice`() {
            val responses = userRepository.users.computeIfAbsent("user") { mutableListOf() }
            responses += User(
                username = "user",
                status = User.Status.BLOCKED,
            )
            responses += User(
                username = "user",
                status = User.Status.ACTIVE,
            )

            expectCatching { service.save("user", "todo name") }.isFailure()
            expectCatching { service.save("user", "todo name") }.isSuccess()
        }
    }
}

class MockUserRepository : UserRepository {
    val users: MutableMap<String, MutableList<User>> = mutableMapOf()
    val calls: MutableMap<String, Int> = mutableMapOf()

    override fun findByName(username: String): User? {
        calls.compute(username) { _, prev -> (prev ?: 0) + 1 }
        val responseIndex = minOf(calls.getValue(username), users[username]?.size ?: 0) - 1
        return users[username]
            ?.get(responseIndex)
    }
}
