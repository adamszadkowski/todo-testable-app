package pl.allegro.agh.distributedsystems.todo.domain.todos

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import pl.allegro.agh.distributedsystems.todo.domain.users.User
import pl.allegro.agh.distributedsystems.todo.domain.users.UserRepository
import pl.allegro.agh.distributedsystems.todo.infrastructure.todos.InMemoryTodosRepository
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isSuccess
import strikt.assertions.message

@ExtendWith(MockKExtension::class)
class TodosServiceTest(
    @RelaxedMockK private val userRepository: UserRepository,
) {
    private val repository = InMemoryTodosRepository()
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
            every { userRepository.findByName(any()) } returns User(
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

        @Nested
        inner class `validate length` {

            @ParameterizedTest
            @CsvSource(
                "  0, TODO is too short",
                "100, TODO is too long",
                "101, TODO is too long",
            )
            fun `fail on incorrect length`(length: Int, message: String) {
                expectThrows<TodosService.CannotSaveException> {
                    service.save(activeUser, "a".repeat(length))
                }.message.isEqualTo(message)
            }

            @ParameterizedTest
            @ValueSource(ints = [1, 2, 98, 99])
            fun `pass on correct length`(length: Int) {
                expectCatching {
                    service.save(activeUser, "a".repeat(length))
                }.isSuccess()
            }
        }
    }

    @Nested
    inner class `user blocking` {

        @Test
        fun `reject saves by missing user`() {
            every { userRepository.findByName(any()) } returns null

            expectThrows<TodosService.CannotSaveException> {
                service.save("user", "todo name")
            }.message.isEqualTo("User is not active")
            verify(exactly = 1) { userRepository.findByName("user") }
        }

        @Test
        fun `reject saves by blocked user`() {
            every { userRepository.findByName(any()) } returns User(
                username = "user",
                status = User.Status.BLOCKED,
            )

            expectThrows<TodosService.CannotSaveException> {
                service.save("user", "")
            }.message.isEqualTo("User is not active")
            verify(exactly = 1) { userRepository.findByName("user") }
        }
    }
}
