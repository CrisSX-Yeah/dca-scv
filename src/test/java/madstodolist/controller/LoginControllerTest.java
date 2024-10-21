package madstodolist.controller;

import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import madstodolist.service.UsuarioServiceException;
import madstodolist.authentication.ManagerUserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
// Import necessary annotations
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// Import necessary mock beans
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

// Add Spring Security Test imports
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// Import necessary matchers
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoginController.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Registro Form - Admin Checkbox Visible When No Admin Exists")
    public void registroForm_NoAdminExists_ShouldShowAdminCheckbox() throws Exception {
        // GIVEN
        when(usuarioService.existeAdministrador()).thenReturn(false);

        // WHEN & THEN
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("formRegistro"))
                .andExpect(model().attributeExists("registroData"))
                .andExpect(model().attribute("adminExists", false))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Registrarse como administrador")));
    }

    @Test
    @DisplayName("Registro Form - Admin Checkbox Hidden When Admin Exists")
    public void registroForm_AdminExists_ShouldHideAdminCheckbox() throws Exception {
        // GIVEN
        when(usuarioService.existeAdministrador()).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("formRegistro"))
                .andExpect(model().attributeExists("registroData"))
                .andExpect(model().attribute("adminExists", true))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Registrarse como administrador"))));
    }

    @Test
    @DisplayName("Registro - Successful Admin Registration")
    public void registro_AdminRegistration_ShouldRedirectToLogin() throws Exception {
        // GIVEN
        when(usuarioService.existeAdministrador()).thenReturn(false);
        RegistroData registroData = new RegistroData();
        registroData.setEmail("admin@ua");
        registroData.setPassword("adminpass");
        registroData.setAdmin(true);

        UsuarioData usuarioData = new UsuarioData();
        usuarioData.setId(1L);
        usuarioData.setEmail("admin@ua");
        usuarioData.setAdmin(true);

        when(usuarioService.registrar(any(RegistroData.class))).thenReturn(usuarioData);

        // WHEN & THEN
        mockMvc.perform(post("/registro")
                        .with(csrf()) // Include CSRF token
                        .param("email", "admin@ua")
                        .param("password", "adminpass")
                        .param("admin", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Registro - Attempt to Register Second Admin")
    public void registro_SecondAdminRegistration_ShouldShowError() throws Exception {
        // GIVEN
        when(usuarioService.existeAdministrador()).thenReturn(true);

        // Simulate exception thrown by service
        when(usuarioService.registrar(any(RegistroData.class))).thenThrow(new UsuarioServiceException("Ya existe un administrador en el sistema"));

        // WHEN & THEN
        mockMvc.perform(post("/registro")
                        .with(csrf()) // Include CSRF token
                        .param("email", "admin2@ua")
                        .param("password", "adminpass2")
                        .param("admin", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("formRegistro"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Ya existe un administrador en el sistema"));
    }

}
