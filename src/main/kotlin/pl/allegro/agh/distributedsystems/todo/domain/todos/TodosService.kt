package pl.allegro.agh.distributedsystems.todo.domain.todos

import pl.allegro.agh.distributedsystems.todo.domain.users.User
import pl.allegro.agh.distributedsystems.todo.domain.users.UserRepository
import java.util.*

class TodosService(
    private val todosRepository: TodosRepository,
    private val userRepository: UserRepository,
) {
    fun getAll(user: String): List<Todo> = todosRepository.getAll(user)

    fun save(user: String, name: String): Todo = when {
        userRepository.findByName(user)?.status != User.Status.ACTIVE -> throw CannotSaveException("User is not active")
        name.isEmpty() -> throw CannotSaveException("TODO is too short")
        name.length >= 100 -> throw CannotSaveException("TODO is too long")
        else -> Todo(
            id = UUID.randomUUID().toString(),
            user = user,
            name = name,
        ).let(todosRepository::save)
    }

    class CannotSaveException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
}
