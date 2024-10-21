package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void servicioLoginUsuarioOK() throws Exception {
        // GIVEN
        UsuarioData anaGarcia = new UsuarioData();
        anaGarcia.setNombre("Ana García");
        anaGarcia.setId(1L);

        when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                .thenReturn(anaGarcia);

        // Create a MockHttpSession to simulate the logged-in user
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", anaGarcia.getId());

        // WHEN, THEN
        this.mockMvc.perform(post("/login")
                        .with(csrf()) // Include CSRF token
                        .param("eMail", "ana.garcia@gmail.com")
                        .param("password", "12345678")
                        .session(session)) // Use the mock session
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/tareas"));
    }

    @Test
    public void servicioLoginUsuarioNotFound() throws Exception {
        // GIVEN
        when(usuarioService.login("pepito.perez@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_NOT_FOUND);

        // Create a MockHttpSession for a logged-out user (session is empty)
        MockHttpSession session = new MockHttpSession();

        // WHEN, THEN
        this.mockMvc.perform(post("/login")
                        .with(csrf()) // Include CSRF token
                        .param("eMail", "pepito.perez@gmail.com")
                        .param("password", "12345678")
                        .session(session)) // Use the mock session
                .andExpect(status().isOk())
                .andExpect(view().name("formLogin")) // Return to login view
                .andExpect(content().string(containsString("No existe usuario")));
    }

    @Test
    public void servicioLoginUsuarioErrorPassword() throws Exception {
        // GIVEN
        when(usuarioService.login("ana.garcia@gmail.com", "000"))
                .thenReturn(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // Create a MockHttpSession for a logged-out user (session is empty)
        MockHttpSession session = new MockHttpSession();

        // WHEN, THEN
        this.mockMvc.perform(post("/login")
                        .with(csrf()) // Include CSRF token
                        .param("eMail", "ana.garcia@gmail.com")
                        .param("password", "000")
                        .session(session)) // Use the mock session
                .andExpect(status().isOk())
                .andExpect(view().name("formLogin")) // Return to login view
                .andExpect(content().string(containsString("Contraseña incorrecta")));
    }

}
