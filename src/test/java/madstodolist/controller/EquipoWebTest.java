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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        // Añadimos dos equipos asociados a ese usuario
        EquipoData equipo1 = equipoService.crearEquipo("Equipo Alpha");
        EquipoData equipo2 = equipoService.crearEquipo("Equipo Beta");

        // Asignamos el usuario a ambos equipos
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());

        // Devolvemos los IDs del usuario y los equipos
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("equipo1Id", equipo1.getId());
        ids.put("equipo2Id", equipo2.getId());

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
}
