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
@WebMvcTest(controllers = {UserController.class, HomeController.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    // **MockBean for ManagerUserSession**
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
    @DisplayName("Listar Usuarios - Admin Puede Acceder y Ver Lista")
    public void listarUsuarios_AdminUser_ShouldReturnUsersList() throws Exception {
        // GIVEN
        // Mock ManagerUserSession to simulate an admin user is logged in
        Long adminId = 1L;
        UsuarioData adminUser = new UsuarioData();
        adminUser.setId(adminId);
        adminUser.setEmail("admin@ua");
        adminUser.setNombre("Admin User");
        adminUser.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(adminUser);
        when(usuarioService.listarUsuarios(any(Pageable.class))).thenReturn(usuariosPage);

        // WHEN & THEN
        mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaUsuarios"))
                .andExpect(model().attributeExists("usuariosPage"))
                .andExpect(model().attribute("usuariosPage", usuariosPage))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user1@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user2@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("admin@ua"))));
    }

    @Test
    @DisplayName("Listar Usuarios con Paginación - Admin Puede Acceder y Ver Página Correcta")
    public void listarUsuarios_WithPagination_AdminUser_ShouldReturnCorrectPage() throws Exception {
        // GIVEN
        // Mock ManagerUserSession to simulate an admin user is logged in
        Long adminId = 1L;
        UsuarioData adminUser = new UsuarioData();
        adminUser.setId(adminId);
        adminUser.setEmail("admin@ua");
        adminUser.setNombre("Admin User");
        adminUser.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(adminUser);

        // Create a paginated list with one user
        UsuarioData user3 = new UsuarioData();
        user3.setId(3L);
        user3.setEmail("user3@example.com");
        user3.setNombre("User Three");

        Page<UsuarioData> page = new PageImpl<>(Arrays.asList(user3), PageRequest.of(1, 1), 3);
        when(usuarioService.listarUsuarios(any(Pageable.class))).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/registrados").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaUsuarios"))
                .andExpect(model().attributeExists("usuariosPage"))
                .andExpect(model().attribute("usuariosPage", page))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user3@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("user1@example.com"))));
    }

    @Test
    @DisplayName("Descripción de Usuario Existente - Admin Puede Acceder y Ver Descripción")
    public void descripcionUsuario_Existente_ShouldReturnUserDescription() throws Exception {
        // GIVEN
        // Mock ManagerUserSession to simulate an admin user is logged in
        Long adminId = 1L;
        UsuarioData adminUser = new UsuarioData();
        adminUser.setId(adminId);
        adminUser.setEmail("admin@ua");
        adminUser.setNombre("Admin User");
        adminUser.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(adminUser);

        // Mock UsuarioService.findById for the target user
        UsuarioData usuario = new UsuarioData();
        usuario.setId(2L);
        usuario.setEmail("user2@ua");
        usuario.setNombre("Usuario Dos");
        usuario.setFechaNacimiento(null); // Optional field

        when(usuarioService.findById(2L)).thenReturn(usuario);

        // WHEN & THEN
        mockMvc.perform(get("/registrados/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarioDescripcion"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attribute("usuario", usuario))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Usuario Dos")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user2@ua")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("password"))));
    }

    @Test
    @DisplayName("Descripción de Usuario No Existente - Admin Acceso Prohibido y Recibe 401")
    public void descripcionUsuario_NoExistente_ShouldReturn401() throws Exception {
        // GIVEN
        // Mock ManagerUserSession to simulate an admin user is logged in
        Long adminId = 1L;
        UsuarioData adminUser = new UsuarioData();
        adminUser.setId(adminId);
        adminUser.setEmail("admin@ua");
        adminUser.setNombre("Admin User");
        adminUser.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(adminUser);

        // Mock UsuarioService.findById(999L) to return null (user does not exist)
        when(usuarioService.findById(999L)).thenReturn(null);

        // WHEN & THEN
        mockMvc.perform(get("/registrados/999"))
                .andExpect(status().isOk()) // The controller should return view "error/404" with status 200
                .andExpect(view().name("error/404"));
    }

    @Test
    @DisplayName("Descripción de Usuario con Fecha de Nacimiento - Admin Puede Ver Fecha")
    public void descripcionUsuario_ConFechaNacimiento_ShouldDisplayFechaNacimiento() throws Exception {
        // GIVEN
        // Mock ManagerUserSession to simulate an admin user is logged in
        Long adminId = 1L;
        UsuarioData adminUser = new UsuarioData();
        adminUser.setId(adminId);
        adminUser.setEmail("admin@ua");
        adminUser.setNombre("Admin User");
        adminUser.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(adminUser);

        // Mock UsuarioService.findById for the target user
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

    @Test
    @DisplayName("Listar Usuarios - Admin Puede Ver Solo Usuarios No Admin")
    public void listarUsuarios_ShouldNotIncludeAdminUser() throws Exception {
        // GIVEN
        // Mock ManagerUserSession to simulate an admin user is logged in
        Long adminId = 1L;
        UsuarioData adminUser = new UsuarioData();
        adminUser.setId(adminId);
        adminUser.setEmail("admin@ua");
        adminUser.setNombre("Admin User");
        adminUser.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(adminUser);

        // Mock UsuarioService.listarUsuarios to return a page of non-admin users
        UsuarioData user1 = new UsuarioData();
        user1.setId(2L);
        user1.setEmail("user1@ua");
        user1.setNombre("User One");
        user1.setAdmin(false);

        UsuarioData user2 = new UsuarioData();
        user2.setId(3L);
        user2.setEmail("user2@ua");
        user2.setNombre("User Two");
        user2.setAdmin(false);

        Page<UsuarioData> usuariosPage = new PageImpl<>(Arrays.asList(user1, user2), PageRequest.of(0, 10), 2);
        when(usuarioService.listarUsuarios(any(Pageable.class))).thenReturn(usuariosPage);

        // WHEN & THEN
        mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaUsuarios"))
                .andExpect(model().attributeExists("usuariosPage"))
                .andExpect(model().attribute("usuariosPage", usuariosPage))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user1@ua")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user2@ua")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("admin@ua"))));
    }

    @Test
    @DisplayName("Acceso protegido - Admin Puede Acceder a /registrados")
    public void accederRegistrados_AdminUser_ShouldAllowAccess() throws Exception {
        // GIVEN
        // Mock ManagerUserSession to return admin user ID
        Long adminId = 1L;
        UsuarioData adminUser = new UsuarioData();
        adminUser.setId(adminId);
        adminUser.setEmail("admin@ua");
        adminUser.setNombre("Admin User");
        adminUser.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(adminUser);

        // Mock UsuarioService.listarUsuarios to return a page of non-admin users
        UsuarioData user1 = new UsuarioData();
        user1.setId(2L);
        user1.setEmail("user1@ua");
        user1.setNombre("User One");
        user1.setAdmin(false);

        UsuarioData user2 = new UsuarioData();
        user2.setId(3L);
        user2.setEmail("user2@ua");
        user2.setNombre("User Two");
        user2.setAdmin(false);

        Page<UsuarioData> usuariosPage = new PageImpl<>(Arrays.asList(user1, user2), PageRequest.of(0, 10), 2);
        when(usuarioService.listarUsuarios(any(Pageable.class))).thenReturn(usuariosPage);

        // WHEN & THEN
        mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaUsuarios"))
                .andExpect(model().attributeExists("usuariosPage"))
                .andExpect(model().attribute("usuariosPage", usuariosPage))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user1@ua")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user2@ua")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("admin@ua"))));
    }

    @Test
    @DisplayName("Acceso protegido - No Admin no Puede Acceder a /registrados")
    public void accederRegistrados_NonAdminUser_ShouldReturnUnauthorized() throws Exception {
        // GIVEN
        // Mock ManagerUserSession to return non-admin user ID
        Long userId = 2L;
        UsuarioData nonAdminUser = new UsuarioData();
        nonAdminUser.setId(userId);
        nonAdminUser.setEmail("user@ua");
        nonAdminUser.setNombre("User Example");
        nonAdminUser.setAdmin(false);
        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(nonAdminUser);

        // WHEN & THEN
        mockMvc.perform(get("/registrados"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("404 - Página No Encontrada")));
    }

    @Test
    @DisplayName("Acceso protegido - No Admin no Puede Acceder a /registrados/{id}")
    public void accederUsuarioDescripcion_NonAdminUser_ShouldReturnUnauthorized() throws Exception {
        // GIVEN
        // Mock ManagerUserSession to return non-admin user ID
        Long userId = 2L;
        UsuarioData nonAdminUser = new UsuarioData();
        nonAdminUser.setId(userId);
        nonAdminUser.setEmail("user@ua");
        nonAdminUser.setNombre("User Example");
        nonAdminUser.setAdmin(false);
        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(nonAdminUser);

        // WHEN & THEN
        mockMvc.perform(get("/registrados/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("404 - Página No Encontrada")));
    }


    @Test
    @DisplayName("Navbar - Admin Usuario Puede Ver 'Registrados' y 'Tasks'")
    public void navbar_AdminUser_ShouldShowRegistradosAndTasks() throws Exception {
        // GIVEN
        Long adminId = 1L;
        UsuarioData adminUser = new UsuarioData();
        adminUser.setId(adminId);
        adminUser.setEmail("admin@ua");
        adminUser.setNombre("Admin User");
        adminUser.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(adminUser);

        // WHEN & THEN
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Registrados")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tasks")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Admin User")));
    }

    @Test
    @DisplayName("Navbar - Usuario No Admin Puede Ver Solo 'Tasks'")
    public void navbar_NonAdminUser_ShouldShowOnlyTasks() throws Exception {
        // GIVEN
        Long userId = 2L;
        UsuarioData nonAdminUser = new UsuarioData();
        nonAdminUser.setId(userId);
        nonAdminUser.setEmail("user@ua");
        nonAdminUser.setNombre("User Example");
        nonAdminUser.setAdmin(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(nonAdminUser);

        // WHEN & THEN
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                // Check that the "Registrados" link does not exist
                .andExpect(xpath("//a[@href='/registrados']").doesNotExist())
                // Check that the "Tasks" link exists
                .andExpect(xpath("//a[@href='/tasks-auth']").exists())
                // Check that the user's name is displayed
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User Example")));
    }

    @Test
    @DisplayName("Navbar - Usuario No Logeado No Puede Ver 'Registrados' ni 'Tasks'")
    public void navbar_UnauthenticatedUser_ShouldNotShowRegistradosOrTasks() throws Exception {
        // GIVEN
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN & THEN
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                // Check that the "Registrados" link does not exist
                .andExpect(xpath("//a[@href='/registrados']").doesNotExist())
                // Check that the "Tasks" link does not exist
                .andExpect(xpath("//a[@href='/tasks-auth']").doesNotExist())
                // Check that "Log out" is not displayed
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Log out"))))
                // Check that "Login" and "Register" are displayed
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Login")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Register")));
    }




}
