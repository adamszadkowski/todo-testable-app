package pl.allegro.agh.distributedsystems.todo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.allegro.agh.distributedsystems.todo.domain.users.UserRepository
import pl.allegro.agh.distributedsystems.todo.infrastructure.users.MongoUserRepository
import pl.allegro.agh.distributedsystems.todo.infrastructure.users.MongoUserRepositoryCRUD

@Configuration
class UsersConfiguration {

    @Bean
    fun userRepository(crud: MongoUserRepositoryCRUD): UserRepository = MongoUserRepository(crud)
}
