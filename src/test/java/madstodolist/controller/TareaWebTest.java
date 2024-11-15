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
        TareaData tarea2 = tareaService.nuevaTareaUsuario(usuario.getId(), "Renovar DNI");
        TareaData tarea3 = tareaService.nuevaTareaUsuario(usuario.getId(), "Renovar DNI");

        tareaService.asignarHoras(tarea1.getId(), 100);
        tareaService.asignarHoras(tarea2.getId(), 200);
        tareaService.asignarHoras(tarea3.getId(), 600);
        tareaService.calcularPromedioHoras(usuario.getId());

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();

        ids.put("usuarioId", usuario.getId());
        ids.put("tareaId", tarea1.getId());
        ids.put("tareaId2", tarea2.getId());
        ids.put("tareaId3", tarea3.getId());

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

    // A partir de aquí son tests creados para verificar la funcionalidad de las horas de las tareas.

    @Test
    public void listaHorasAsignadasYPromedioHorasTareas() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId1 = ids.get("tareaId");
        Long tareaId2 = ids.get("tareaId2");
        Long tareaId3 = ids.get("tareaId3");


        // Moqueamos el método usuarioLogeado para que devuelva el usuario 1L,
        // el mismo que se está usando en la petición. De esta forma evitamos
        // que salte la excepción de que el usuario que está haciendo la
        // petición no está logeado.
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición DELETE para borrar una tarea,
        // se devuelve el estado HTTP que se devuelve es OK,

        String url = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(get(url))
                .andExpect((content().string(allOf(
                        containsString("100"),
                        containsString("200"),
                        containsString("600"),
                        containsString("Promedio de las horas de todas las tareas:"),
                        containsString("300")
                ))));
    }


    @Test
    public void asignarHorasALaTareaActualizaElNumeroDeHorasEnElHTML() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos una petición POST al endpoint para asignar horas a una tarea

        String urlAsignarHoras = "/tareas/" + tareaLavarCocheId + "/asignarHoras";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        // Send POST request with CSRF and authenticated user
        this.mockMvc.perform(post(urlAsignarHoras)
                        .with(csrf()) // Add CSRF token for security
                        .with(user("user@ua")) // Simulate logged-in user
                        .param("hours", "13"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // Y si realizamos un listado de las tareas del usuario
        // ha cambiado el número de horas asignadas de la tarea modificada

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado))
                .andExpect(content().string(containsString("13")));

    }

}
