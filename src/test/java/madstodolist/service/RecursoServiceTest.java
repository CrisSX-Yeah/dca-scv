package madstodolist.service;

import madstodolist.dto.RegistroData;
import madstodolist.dto.RecursoData;
import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class RecursoServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    RecursoService recursoService;

    // Método para inicializar los datos de prueba en la BD
    Map<String, Long> addUsuarioRecursosBD() {
        // Añadimos un usuario a la base de datos
        RegistroData registroData = new RegistroData();
        registroData.setEmail("user@ua");
        registroData.setPassword("123");
        UsuarioData usuarioNuevo = usuarioService.registrar(registroData);

        // Y añadimos dos recursos asociados a ese usuario
        RecursoData recurso1 = recursoService.nuevoRecursoUsuario(usuarioNuevo.getId(), "Libro de Java");
        recursoService.nuevoRecursoUsuario(usuarioNuevo.getId(), "Curso de Spring");

        // Devolvemos los ids del usuario y de la primera recurso añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("recursoId", recurso1.getId());
        return ids;
    }

    @Test
    public void testNuevoRecursoUsuario() {
        // GIVEN
        Long usuarioId = addUsuarioRecursosBD().get("usuarioId");

        // WHEN
        RecursoData nuevoRecurso = recursoService.nuevoRecursoUsuario(usuarioId, "Libro de Spring");

        // THEN
        List<RecursoData> recursos = recursoService.allRecursosUsuario(usuarioId);
        assertThat(recursos).hasSize(3);
        assertThat(recursos).contains(nuevoRecurso);
    }

    @Test
    public void testBuscarRecurso() {
        Long recursoId = addUsuarioRecursosBD().get("recursoId");
        RecursoData libroJava = recursoService.findById(recursoId);
        assertThat(libroJava).isNotNull();
        assertThat(libroJava.getNombre()).isEqualTo("Libro de Java");
    }

    @Test
    public void testModificarRecurso() {
        Map<String, Long> ids = addUsuarioRecursosBD();
        Long usuarioId = ids.get("usuarioId");
        Long recursoId = ids.get("recursoId");

        recursoService.modificaRecurso(recursoId, "Libro de Spring Boot");

        RecursoData recursoBD = recursoService.findById(recursoId);
        assertThat(recursoBD.getNombre()).isEqualTo("Libro de Spring Boot");

        List<RecursoData> recursos = recursoService.allRecursosUsuario(usuarioId);
        assertThat(recursos).contains(recursoBD);
    }

    @Test
    public void testBorrarRecurso() {
        Map<String, Long> ids = addUsuarioRecursosBD();
        Long usuarioId = ids.get("usuarioId");
        Long recursoId = ids.get("recursoId");

        recursoService.borraRecurso(recursoId);

        assertThat(recursoService.findById(recursoId)).isNull();

        List<RecursoData> recursos = recursoService.allRecursosUsuario(usuarioId);
        assertThat(recursos).hasSize(1);
    }

    @Test
    public void asignarRecursoAUsuario() {
        Map<String, Long> ids = addUsuarioRecursosBD();
        Long usuarioId = ids.get("usuarioId");
        Long recursoId = ids.get("recursoId");

        assertThat(recursoService.usuarioContieneRecurso(usuarioId, recursoId)).isTrue();
    }
}
