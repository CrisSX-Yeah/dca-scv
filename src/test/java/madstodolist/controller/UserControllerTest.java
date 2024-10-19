package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
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

}
