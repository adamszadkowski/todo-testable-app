package pl.allegro.agh.distributedsystems.todo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    fun userDetailsService(): UserDetailsService = InMemoryUserDetailsManager()
        .apply {
            createUser(
                User.withUsername("user")
                    .password("{noop}password")
                    .roles("USER")
                    .build()
            )
        }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf().disable()
        .authorizeRequests {
            it.antMatchers("/todos/**").authenticated()
        }
        .httpBasic()
        .and()
        .build()
}
