package pl.allegro.agh.distributedsystems.todo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.SecurityFilterChain
import pl.allegro.agh.distributedsystems.todo.domain.users.UserRepository
import pl.allegro.agh.distributedsystems.todo.infrastructure.users.PropertiesUserDetailsService

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService =
        PropertiesUserDetailsService(userRepository)

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }
        .authorizeHttpRequests {
            it
                .requestMatchers("/todos/**", "/users/**").authenticated()
                .requestMatchers(
                    "/swagger-ui/**",
                    "v3/api-docs/**",
                    "/v2/api-docs/**",
                    "/swagger-resources/**",
                ).permitAll()
        }
        .httpBasic { }
        .build()
}
