package pl.allegro.agh.distributedsystems.todo.api

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.allegro.agh.distributedsystems.todo.domain.users.User
import pl.allegro.agh.distributedsystems.todo.domain.users.UserRepository

@RestController
@RequestMapping("/users")
class UsersEndpoint(
    private val userRepository: UserRepository,
) {
    @PostMapping(consumes = ["application/json"])
    fun createUser(@RequestBody user: CreateUser) {
        userRepository.save(User(username = user.user, password = user.password, status = User.Status.ACTIVE))
    }
}

data class CreateUser(
    val user: String,
    val password: String,
)
