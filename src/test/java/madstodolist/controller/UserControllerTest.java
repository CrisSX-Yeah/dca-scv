package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// Import necessary matchers
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    // **Add this MockBean**
    @MockBean
    private ManagerUserSession managerUserSession;

    private Page<UsuarioData> usuariosPage;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        UsuarioData user1 = new UsuarioData();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setNombre("User One");

        UsuarioData user2 = new UsuarioData();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setNombre("User Two");

        usuariosPage = new PageImpl<>(Arrays.asList(user1, user2), PageRequest.of(0, 10), 2);
    }

    @Test
    public void listarUsuarios_ShouldReturnUsersList() throws Exception {
        when(usuarioService.listarUsuarios(any(Pageable.class))).thenReturn(usuariosPage);

        mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaUsuarios"))
                .andExpect(model().attributeExists("usuariosPage"))
                .andExpect(model().attribute("usuariosPage", usuariosPage))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user1@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user2@example.com")));
    }

    @Test
    public void listarUsuarios_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Create a paginated list with one user
        UsuarioData user3 = new UsuarioData();
        user3.setId(3L);
        user3.setEmail("user3@example.com");
        user3.setNombre("User Three");

        Page<UsuarioData> page = new PageImpl<>(Arrays.asList(user3), PageRequest.of(1, 1), 3);
        when(usuarioService.listarUsuarios(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/registrados").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaUsuarios"))
                .andExpect(model().attributeExists("usuariosPage"))
                .andExpect(model().attribute("usuariosPage", page))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user3@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("user1@example.com"))));
    }

    @Test
    @DisplayName("Descripción de Usuario Existente")
    public void descripcionUsuario_Existente_ShouldReturnUserDescription() throws Exception {
        // GIVEN
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setEmail("user1@ua");
        usuario.setNombre("Usuario Uno");
        usuario.setFechaNacimiento(null); // Optional field

        when(usuarioService.findById(1L)).thenReturn(usuario);

        // WHEN & THEN
        mockMvc.perform(get("/registrados/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarioDescripcion"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attribute("usuario", usuario))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Usuario Uno")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user1@ua")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("password"))));
    }

    @Test
    @DisplayName("Descripción de Usuario No Existente")
    public void descripcionUsuario_NoExistente_ShouldReturn404() throws Exception {
        // GIVEN
        when(usuarioService.findById(999L)).thenReturn(null);

        // WHEN & THEN
        mockMvc.perform(get("/registrados/999"))
                .andExpect(status().isOk()) // Depending on your error handling, this might be a redirect or a 404 status
                .andExpect(view().name("error/404"));
    }

    @Test
    @DisplayName("Descripción de Usuario con Fecha de Nacimiento")
    public void descripcionUsuario_ConFechaNacimiento_ShouldDisplayFechaNacimiento() throws Exception {
        // GIVEN
        UsuarioData usuario = new UsuarioData();
        usuario.setId(3L);
        usuario.setEmail("user3@ua");
        usuario.setNombre("Usuario Tres");
        usuario.setFechaNacimiento(new java.util.Date(90, 0, 1)); // 01/01/1990

        when(usuarioService.findById(3L)).thenReturn(usuario);

        // WHEN & THEN
        mockMvc.perform(get("/registrados/3"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarioDescripcion"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attribute("usuario", usuario))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("01/01/1990")));
    }


}
