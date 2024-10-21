package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.RegistroData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Import CSRF
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; // Import user
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class TareaWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    // Método para inicializar los datos de prueba en la BD
    Map<String, Long> addUsuarioTareasBD() {
        // Añadimos un usuario a la base de datos
        RegistroData registroData = new RegistroData();
        registroData.setEmail("user@ua");
        registroData.setPassword("123");
        UsuarioData usuario = usuarioService.registrar(registroData);

        // Y añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuario.getId(), "Lavar coche");
        tareaService.nuevaTareaUsuario(usuario.getId(), "Renovar DNI");

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;
    }

    @Test
    public void listaTareas() throws Exception {
        // GIVEN
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String url = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(get(url).with(user("user@ua")))
                .andExpect(content().string(allOf(
                        containsString("Lavar coche"),
                        containsString("Renovar DNI")
                )));
    }

    @Test
    public void getNuevaTareaDevuelveForm() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPeticion = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlAction = "action=\"/usuarios/" + usuarioId.toString() + "/tareas/nueva\"";

        this.mockMvc.perform(get(urlPeticion).with(user("user@ua")))
                .andExpect(content().string(allOf(
                        containsString("form method=\"post\""),
                        containsString(urlAction)
                )));
    }

    @Test
    public void postNuevaTareaDevuelveRedirectYAñadeTarea() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPost = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlRedirect = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(post(urlPost)
                        .with(csrf())
                        .with(user("user@ua"))
                        .param("titulo", "Estudiar examen MADS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect).with(user("user@ua")))
                .andExpect(content().string(containsString("Estudiar examen MADS")));
    }

    @Test
    public void postDeleteTareaDevuelveRedirectyBorraTarea() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlDelete = "/tareas/" + tareaLavarCocheId.toString() + "/borrar";
        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlDelete)
                        .with(csrf())
                        .with(user("user@ua")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlListado));

        this.mockMvc.perform(get(urlListado).with(user("user@ua")))
                .andExpect(content().string(allOf(
                        not(containsString("Lavar coche")),
                        containsString("Renovar DNI"))));
    }

    @Test
    public void editarTareaActualizaLaTarea() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlEditar = "/tareas/" + tareaLavarCocheId + "/editar";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlEditar)
                        .with(csrf())
                        .with(user("user@ua"))
                        .param("titulo", "Limpiar cristales coche"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado).with(user("user@ua")))
                .andExpect(content().string(containsString("Limpiar cristales coche")));
    }

}
