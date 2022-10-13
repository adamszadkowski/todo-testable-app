package pl.allegro.agh.distributedsystems.todo.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class TodosEndpointTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `get empty todos`() {
        mockMvc.get("/todos").andExpect {
            status { is2xxSuccessful() }
            content { json(""" { "todos": [] } """) }
        }
    }
}
