package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.RegistroData;
import madstodolist.dto.RecursoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.RecursoService;
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
public class RecursoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecursoService recursoService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    // Método para inicializar los datos de prueba en la BD
    Map<String, Long> addUsuarioRecursosBD() {
        // Añadimos un usuario a la base de datos
        RegistroData registroData = new RegistroData();
        registroData.setEmail("user@ua");
        registroData.setPassword("123");
        UsuarioData usuario = usuarioService.registrar(registroData);

        // Y añadimos dos recursos asociados a ese usuario
        RecursoData recurso1 = recursoService.nuevoRecursoUsuario(usuario.getId(), "Libro de Java");
        recursoService.nuevoRecursoUsuario(usuario.getId(), "Curso de Spring");

        // Devolvemos los ids del usuario y de la primera recurso añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("recursoId", recurso1.getId());
        return ids;
    }

    @Test
    public void listaRecursos() throws Exception {
        // GIVEN
        Long usuarioId = addUsuarioRecursosBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String url = "/usuarios/" + usuarioId.toString() + "/recursos";

        this.mockMvc.perform(get(url).with(user("user@ua")))
                .andExpect(content().string(allOf(
                        containsString("Libro de Java"),
                        containsString("Curso de Spring")
                )));
    }

    @Test
    public void getNuevoRecursoDevuelveForm() throws Exception {
        Long usuarioId = addUsuarioRecursosBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPeticion = "/usuarios/" + usuarioId.toString() + "/recursos/nuevo";
        String urlAction = "/usuarios/" + usuarioId.toString() + "/recursos/nuevo";

        this.mockMvc.perform(get(urlPeticion).with(user("user@ua")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("<form"),
                        containsString("method=\"post\""),
                        containsString("action=\"" + urlAction + "\"")
                )));
    }

    @Test
    public void postNuevoRecursoDevuelveRedirectYAñadeRecurso() throws Exception {
        Long usuarioId = addUsuarioRecursosBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPost = "/usuarios/" + usuarioId.toString() + "/recursos/nuevo";
        String urlRedirect = "/usuarios/" + usuarioId.toString() + "/recursos";

        this.mockMvc.perform(post(urlPost)
                        .with(csrf())
                        .with(user("user@ua"))
                        .param("nombre", "Libro de Spring"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect).with(user("user@ua")))
                .andExpect(content().string(containsString("Libro de Spring")));
    }

    @Test
    public void postDeleteRecursoDevuelveRedirectyBorraRecurso() throws Exception {
        Map<String, Long> ids = addUsuarioRecursosBD();
        Long usuarioId = ids.get("usuarioId");
        Long recursoLibroJavaId = ids.get("recursoId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlDelete = "/recursos/" + recursoLibroJavaId.toString() + "/borrar";
        String urlListado = "/usuarios/" + usuarioId + "/recursos";

        this.mockMvc.perform(post(urlDelete)
                        .with(csrf())
                        .with(user("user@ua")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlListado));

        this.mockMvc.perform(get(urlListado).with(user("user@ua")))
                .andExpect(content().string(allOf(
                        not(containsString("Libro de Java")),
                        containsString("Curso de Spring")
                )));
    }

    @Test
    public void editarRecursoActualizaElRecurso() throws Exception {
        Map<String, Long> ids = addUsuarioRecursosBD();
        Long usuarioId = ids.get("usuarioId");
        Long recursoLibroJavaId = ids.get("recursoId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlEditar = "/recursos/" + recursoLibroJavaId + "/editar";
        String urlRedirect = "/usuarios/" + usuarioId + "/recursos";

        this.mockMvc.perform(post(urlEditar)
                        .with(csrf())
                        .with(user("user@ua"))
                        .param("nombre", "Libro de Spring Boot"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        String urlListado = "/usuarios/" + usuarioId + "/recursos";

        this.mockMvc.perform(get(urlListado).with(user("user@ua")))
                .andExpect(content().string(containsString("Libro de Spring Boot")));
    }
}
