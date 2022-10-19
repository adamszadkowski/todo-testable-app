package pl.allegro.agh.distributedsystems.todo.api

import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
class TodosEndpointTest(@Autowired private val mockMvc: MockMvc) {

    @BeforeEach
    fun `create users`() {
        createUser("user")
        createUser("user1")
        createUser("user2")
    }

    @Test
    fun `get empty todos`() {
        getTodos(user = "user")
            .andExpect {
                jsonPath("\$.todos", emptyIterable<Any>())
            }
    }

    @Test
    fun `deny access on missing authentication`() {
        mockMvc.get("/todos").andExpect { status { isUnauthorized() } }
        mockMvc.post("/todos").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `save single todo`() {
        saveTodo(user = "user", "new todo")
            .andExpect {
                jsonPath("\$.name", equalTo("new todo"))
                jsonPath("\$.id", matchesRegex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$"))
            }
    }

    @Nested
    inner class `single saved` {

        @BeforeEach
        fun `save single todo`() {
            saveTodo(user = "user", "new todo")
        }

        @Test
        fun `get id`() {
            getTodos(user = "user")
                .andExpect {
                    jsonPath(
                        "\$.todos[0].id",
                        matchesRegex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")
                    )
                }
        }

        @Test
        fun `get all for user`() {
            getTodos(user = "user")
                .andExpect {
                    jsonPath("\$.todos.*.name", contains("new todo"))
                }
        }

        @Test
        fun `save second todo`() {
            saveTodo(user = "user", "second todo")

            getTodos(user = "user")
                .andExpect {
                    jsonPath("\$.todos.*.name", contains("new todo", "second todo"))
                }
        }
    }

    private fun createUser(user: String) {
        mockMvc.post("/users") {
            with(user("admin"))
            contentType = MediaType.APPLICATION_JSON
            content = """ { "user": "$user", "password": "{noop}pass" } """
        }.andExpect {
            status { is2xxSuccessful() }
        }
    }

    private fun getTodos(user: String) =
        mockMvc.get("/todos") { with(user(user)) }
            .andExpect { status { is2xxSuccessful() } }

    private fun saveTodo(user: String, name: String) =
        mockMvc.post("/todos") {
            with(user(user))
            contentType = MediaType.APPLICATION_JSON
            content = """ { "name": "$name" } """
        }.andExpect {
            status { is2xxSuccessful() }
        }
}
