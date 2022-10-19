package pl.allegro.agh.distributedsystems.todo.domain.users

interface UserRepository {
    fun save(user: User)
    fun findByName(username: String): User?
}

data class User(
    val username: String,
    val password: String = "",
    val status: Status = Status.ACTIVE,
) {
    enum class Status { ACTIVE, BLOCKED }
}
