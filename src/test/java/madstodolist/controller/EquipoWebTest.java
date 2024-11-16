// EquipoWebTest.java

package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.dto.RegistroData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    /**
     * Método para inicializar los datos de prueba en la BD.
     * Crea un usuario y dos equipos asociados a ese usuario.
     */
    Map<String, Long> addUsuarioEquiposBD() {
        // Añadimos un usuario a la base de datos
        RegistroData registroData = new RegistroData();
        registroData.setEmail("usuario@ua.com");
        registroData.setPassword("password");
        UsuarioData usuario = usuarioService.registrar(registroData);

        RegistroData registroDataAdmin = new RegistroData();
        registroDataAdmin.setEmail("admin@admin.com");
        registroDataAdmin.setPassword("admin");
        registroDataAdmin.setAdmin(true);
        UsuarioData usuarioAdmin = usuarioService.registrar(registroDataAdmin);

        // Añadimos dos equipos asociados a ese usuario
        EquipoData equipo1 = equipoService.crearEquipo("Equipo Alpha");
        EquipoData equipo2 = equipoService.crearEquipo("Equipo Beta");

        // Asignamos el usuario a ambos equipos
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());

        Long equipoNoExistenteId = 999L;

        // Devolvemos los IDs del usuario y los equipos
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("usuarioAdmin", usuarioAdmin.getId());
        ids.put("equipo1Id", equipo1.getId());
        ids.put("equipo2Id", equipo2.getId());
        ids.put("equipoNoExistenteId", equipoNoExistenteId);

        return ids;
    }

    /**
     * Test para verificar que el listado de equipos se muestra correctamente.
     */
    @Test
    public void listaEquipos() throws Exception {
        // GIVEN
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String url = "/logeados/equipos";

        this.mockMvc.perform(get(url).with(user("usuario@ua.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("listaEquipos"))
                .andExpect(model().attributeExists("equipos"))
                .andExpect(model().attribute("equipos", hasSize(2)))
                .andExpect(model().attributeExists("equiposPertenecientes"))
                .andExpect(content().string(allOf(
                        containsString("Equipo Alpha"),
                        containsString("Equipo Beta")
                )));
    }

    /**
     * Test para verificar que el listado de miembros de un equipo se muestra correctamente.
     */
    @Test
    public void listaMiembrosEquipo() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipo1Id = ids.get("equipo1Id");
        Long equipo2Id = ids.get("equipo2Id");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlEquipo1 = "/logeados/equipos/" + equipo1Id + "/miembros";
        String urlEquipo2 = "/logeados/equipos/" + equipo2Id + "/miembros";

        // Test para Equipo Alpha
        this.mockMvc.perform(get(urlEquipo1).with(user("usuario@ua.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("listaUsuariosDeUnEquipo"))
                .andExpect(model().attributeExists("usuarios"))
                .andExpect(model().attribute("usuarios", hasSize(1)))
                .andExpect(content().string(allOf(
                        containsString("usuario@ua.com"),
                        containsString("usuario@ua.com")
                )));

        // Test para Equipo Beta
        this.mockMvc.perform(get(urlEquipo2).with(user("usuario@ua.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("listaUsuariosDeUnEquipo"))
                .andExpect(model().attributeExists("usuarios"))
                .andExpect(model().attribute("usuarios", hasSize(1)))
                .andExpect(content().string(allOf(
                        containsString("usuario@ua.com"),
                        containsString("usuario@ua.com")
                )));
    }

    /**
     * Opcional: Test para verificar el manejo de equipos que no existen.
     */
    @Test
    public void listaMiembrosEquipoNoExistente() throws Exception {
        // GIVEN
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        Long equipoNoExistenteId = 999L; // ID que no existe
        String url = "/logeados/equipos/" + equipoNoExistenteId + "/miembros";

        this.mockMvc.perform(get(url).with(user("usuario@ua.com")))
                .andExpect(status().isNotFound()) // Espera un estado 404
                .andExpect(view().name("error/404")) // Espera la vista de error 404
                .andExpect(model().attributeExists("errorMessage")) // Verifica que el modelo tiene 'errorMessage'
                .andExpect(content().string(containsString("El equipo con id 999no existe"))); // Verifica el mensaje de error
    }

    @Test
    public void getNuevoEquipoDevuelveForm() throws Exception {
        // GIVEN
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPeticion = "/logeados/equipos/nuevo-equipo";
        String urlAction = "action=\"/logeados/equipos/nuevo-equipo\"";

        this.mockMvc.perform(get(urlPeticion).with(user("usuario@ua.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("formNuevoEquipo"))
                .andExpect(content().string(allOf(
                        containsString("Nuevo Equipo"),
                        containsString("Nombre del equipo"),
                        containsString("Crear Equipo"),
                        containsString("Cancelar")
                )));
    }

    @Test
    public void postNuevoEquipoDevuelveRedirectYAgregaEquipo() throws Exception {
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPeticion = "/logeados/equipos/nuevo-equipo";
        String urlRedirect = "/logeados/equipos";

        this.mockMvc.perform(post(urlPeticion)
                        .with(csrf())
                        .with(user("usuario@ua.com"))
                        .param("nombre", "Nuevo Equipo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect).with(user("usuario@ua.com")))
                .andExpect(content().string(containsString("Nuevo Equipo")));
    }

    @Test
    public void agregarYLuegoEliminaUsuarioLogeadoDeEquipo() throws Exception {
        // Configurar datos iniciales
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipo1Id = ids.get("equipo1Id");
        Long equipo2Id = ids.get("equipo2Id");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);


        String urlRedirect = "/logeados/equipos";
        // Simular agregar usuario al equipo
        String urlAgregar = "/logeados/equipos/" + equipo1Id + "/agrega-usuario-logeado/" + usuarioId;
        String urlAgregar2 = "/logeados/equipos/" + equipo2Id + "/agrega-usuario-logeado/" + usuarioId;

        this.mockMvc.perform(post(urlAgregar)
                        .with(csrf())
                        .with(user("usuario@ua.com")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(post(urlAgregar2)
                        .with(csrf())
                        .with(user("usuario@ua.com")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // Verificar que el botón "Eliminarme del Equipo" está presente
        this.mockMvc.perform(get(urlRedirect).with(user("usuario@ua.com")))
                .andExpect(content().string(containsString("Eliminarme del Equipo")))
                .andExpect(content().string(not(containsString("Agregarme al Equipo"))));

        // Simular eliminar usuario del equipo
        String urlEliminar = "/logeados/equipos/" + equipo1Id + "/elimina-usuario-logeado/" + usuarioId;
        String urlEliminar2 = "/logeados/equipos/" + equipo2Id + "/elimina-usuario-logeado/" + usuarioId;

        this.mockMvc.perform(post(urlEliminar)
                        .with(csrf())
                        .with(user("usuario@ua.com")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(post(urlEliminar2)
                        .with(csrf())
                        .with(user("usuario@ua.com")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // IMPORTANTE: Realizar otra solicitud GET para actualizar el estado de la página
        this.mockMvc.perform(get(urlRedirect).with(user("usuario@ua.com")))
                .andExpect(content().string(containsString("Agregarme al Equipo")))
                .andExpect(content().string(not(containsString("Eliminarme del Equipo"))));
    }

    @Test
    public void formEditarEquipo() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioAdminId = ids.get("usuarioAdmin");
        Long equipoId = ids.get("equipo1Id");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdminId);

        String url = "/admin/auth/equipos/" + equipoId + "/editar";

        this.mockMvc.perform(get(url)
                        .with(user("admin@admin.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("formEditarEquipo"))
                .andExpect(model().attributeExists("equipo"))
                .andExpect(model().attribute("equipo", hasProperty("id", is(equipoId))))
                .andExpect(model().attribute("equipo", hasProperty("nombre", is("Equipo Alpha"))));
    }

    @Test
    public void editarEquipo() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioAdminId = ids.get("usuarioAdmin");
        Long equipoId = ids.get("equipo1Id");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdminId);

        String urlPeticion = "/admin/auth/equipos/" + equipoId + "/editar";
        String urlRedirect = "/logeados/equipos";

        // WHEN
        this.mockMvc.perform(post(urlPeticion)
                        .with(csrf())
                        .with(user("admin@admin.com"))
                        .param("nombre", "Equipo Alpha Modificado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // THEN
        this.mockMvc.perform(get(urlRedirect).with(user("admin@admin.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("listaEquipos"))
                .andExpect(content().string(containsString("Equipo Alpha Modificado")));
    }

    @Test
    public void borrarEquipo() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioAdminId = ids.get("usuarioAdmin");
        Long equipoId = ids.get("equipo1Id");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdminId);

        String urlPeticion = "/admin/auth/equipos/" + equipoId + "/borrar";
        String urlRedirect = "/logeados/equipos";

        // WHEN
        this.mockMvc.perform(post(urlPeticion)
                        .with(csrf())
                        .with(user("admin@admin.com")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect))
                .andExpect(flash().attribute("mensaje", "Equipo borrado correctamente"));

        // THEN
        this.mockMvc.perform(get(urlRedirect).with(user("admin@admin.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("listaEquipos"))
                .andExpect(content().string(not(containsString("Equipo Alpha"))));
    }

    @Test
    public void listadoMiembrosEquipoNoExistente() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioAdminId = ids.get("usuarioAdmin");
        Long equipoNoExistenteId = ids.get("equipoNoExistenteId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdminId);

         // ID que no existe
        String url = "/logeados/equipos/" + equipoNoExistenteId + "/miembros";

        // WHEN & THEN
        this.mockMvc.perform(get(url)
                        .with(user("admin@admin.com").roles("ADMIN")))
                .andExpect(status().isNotFound()) // Espera un estado 404
                .andExpect(view().name("error/404")) // Espera la vista de error 404
                .andExpect(model().attributeExists("errorMessage")) // Verifica que el modelo tiene 'errorMessage'
                .andExpect(content().string(containsString("El equipo con id " + equipoNoExistenteId + "no existe"))); // Verifica el mensaje de error
    }

    @Test
    public void agregarUsuarioLogeadoEnEquipoNoExistente() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioAdminId = ids.get("usuarioAdmin");
        Long usuarioId = ids.get("usuarioId");
        Long equipoNoExistenteId = ids.get("equipoNoExistenteId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdminId);


        String urlPeticion = "/logeados/equipos/" + equipoNoExistenteId + "/agrega-usuario-logeado/" + usuarioId;

        // WHEN & THEN
        this.mockMvc.perform(post(urlPeticion)
                        .with(csrf())
                        .with(user("admin@admin.com")))
                .andExpect(status().isNotFound()) // Espera un estado 404
                .andExpect(view().name("error/404")) // Espera la vista de error 404
                .andExpect(model().attributeExists("errorMessage")) // Verifica que el modelo tiene 'errorMessage'
                .andExpect(content().string(containsString("El equipo con id " + equipoNoExistenteId + "no existe"))); // Verifica el mensaje de error
    }

    @Test
    public void eliminaUsuarioLogeadoDeEquipoNoExistente() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioAdminId = ids.get("usuarioAdmin");
        Long usuarioId = ids.get("usuarioId");
        Long equipoNoExistenteId = ids.get("equipoNoExistenteId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdminId);

        String urlPeticion = "/logeados/equipos/" + equipoNoExistenteId + "/elimina-usuario-logeado/" + usuarioId;

        // WHEN & THEN
        this.mockMvc.perform(post(urlPeticion)
                        .with(csrf())
                        .with(user("admin@admin.com")))
                .andExpect(status().isNotFound()) // Espera un estado 404
                .andExpect(view().name("error/404")) // Espera la vista de error 404
                .andExpect(model().attributeExists("errorMessage")) // Verifica que el modelo tiene 'errorMessage'
                .andExpect(content().string(containsString("El equipo con id " + equipoNoExistenteId + "no existe"))); // Verifica el mensaje de error
    }

    @Test
    public void formEditarEquipoNoExistente() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioAdminId = ids.get("usuarioAdmin");
        Long equipoNoExistenteId = ids.get("equipoNoExistenteId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdminId);

        String url = "/admin/auth/equipos/" + equipoNoExistenteId + "/editar";

        // WHEN & THEN
        this.mockMvc.perform(get(url)
                        .with(user("admin@admin.com")))
                .andExpect(status().isNotFound()) // Espera un estado 404
                .andExpect(view().name("error/404")) // Espera la vista de error 404
                .andExpect(model().attributeExists("errorMessage")) // Verifica que el modelo tiene 'errorMessage'
                .andExpect(content().string(containsString("El equipo con id " + equipoNoExistenteId + "no existe"))); // Verifica el mensaje de error
    }

    @Test
    public void editarEquipoNoExistente() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioAdminId = ids.get("usuarioAdmin");
        Long equipoNoExistenteId = ids.get("equipoNoExistenteId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdminId);

        String urlPeticion = "/admin/auth/equipos/" + equipoNoExistenteId + "/editar";

        // WHEN & THEN
        this.mockMvc.perform(post(urlPeticion)
                        .with(csrf())
                        .with(user("admin@admin.com"))
                        .param("nombre", "Nombre Modificado"))
                .andExpect(status().isNotFound()) // Espera un estado 404
                .andExpect(view().name("error/404")) // Espera la vista de error 404
                .andExpect(model().attributeExists("errorMessage")) // Verifica que el modelo tiene 'errorMessage'
                .andExpect(content().string(containsString("El equipo con id " + equipoNoExistenteId + "no existe"))); // Verifica el mensaje de error
    }

    @Test
    public void borrarEquipoNoExistente() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquiposBD();
        Long usuarioAdminId = ids.get("usuarioAdmin");
        Long equipoNoExistenteId = ids.get("equipoNoExistenteId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdminId);

        String urlPeticion = "/admin/auth/equipos/" + equipoNoExistenteId + "/borrar";

        // WHEN & THEN
        this.mockMvc.perform(post(urlPeticion)
                        .with(csrf())
                        .with(user("admin@admin.com").roles("ADMIN")))
                .andExpect(status().isNotFound()) // Espera un estado 404
                .andExpect(view().name("error/404")) // Espera la vista de error 404
                .andExpect(model().attributeExists("errorMessage")) // Verifica que el modelo tiene 'errorMessage'
                .andExpect(content().string(containsString("El equipo con id " + equipoNoExistenteId + "no existe"))); // Verifica el mensaje de error
    }

}
