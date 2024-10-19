package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {HomeController.class, TareaController.class, LoginController.class})
@Import(GlobalExceptionHandler.class) // Import the exception handler
public class MenuBarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    private UsuarioData usuario;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("Cristian");
    }

    @Test
    public void menuBarShouldShowLoginAndRegisterWhenNotLoggedIn() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Login")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Register")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Cristian"))));
    }

    @Test
    public void menuBarShouldShowUsernameWhenLoggedIn() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cristian")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Log out")));
    }

    @Test
    public void tasksLinkShouldRedirectToLoginWhenNotLoggedIn() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/usuarios/1/tareas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=not_logged_in"));
    }

    @Test
    public void tasksLinkShouldShowTasksWhenLoggedIn() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/1/tareas"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Listado de tareas de Cristian")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ToDoList")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tasks")));
    }
}
