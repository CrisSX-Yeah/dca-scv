package madstodolist.service;

import madstodolist.dto.RegistroData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import madstodolist.dto.EquipoData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {


    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    // Método para inicializar los datos de prueba en la BD
    Map<String, Long> addUsuariosEquiposBD() {
        // Añadimos dos usuarios a la base de datos

        RegistroData initialRegister = new RegistroData();
        initialRegister.setEmail("initialRegister@ua");
        initialRegister.setPassword("123");
        UsuarioData initialUserData = usuarioService.registrar(initialRegister);

        RegistroData registroData = new RegistroData();
        registroData.setEmail("user@ua");
        registroData.setPassword("123");
        UsuarioData usuarioNuevo = usuarioService.registrar(registroData);

        // Y añadimos tres equipos asociados a ese usuario

        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");
        EquipoData equipo3 = equipoService.crearEquipo("Proyecto 3");

        //Y añadimos los dos usuarios a cada equipo

        equipoService.añadirUsuarioAEquipo(equipo1.getId(),initialUserData.getId());

        equipoService.añadirUsuarioAEquipo(equipo2.getId(), initialUserData.getId());

        equipoService.añadirUsuarioAEquipo(equipo3.getId(), initialUserData.getId());


        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("initialUserId", initialUserData.getId());
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("equipoId", equipo1.getId());
        ids.put("equipoId2", equipo2.getId());
        ids.put("equipoId3", equipo3.getId());
        return ids;
    }

    @Test
    public void crearRecuperarEquipo() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto CCC");
        equipoService.crearEquipo("Proyecto AAA");
        equipoService.crearEquipo("Proyecto EEE");
        equipoService.crearEquipo("Proyecto DDD");

        // WHEN
        // Recuperamos los equipos
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(5);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
        assertThat(equipos.get(2).getNombre()).isEqualTo("Proyecto CCC");
        assertThat(equipos.get(3).getNombre()).isEqualTo("Proyecto DDD");
        assertThat(equipos.get(4).getNombre()).isEqualTo("Proyecto EEE");
    }

    @Test
    public void añadirUsuarioAEquipo() {
        // GIVEN
        // A new user and a team in the database
        Map<String, Long> ids = addUsuariosEquiposBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipoId = ids.get("equipoId");

        // WHEN
        // Add the user to the team
        equipoService.añadirUsuarioAEquipo(equipoId, usuarioId);

        // THEN
        // Check that the user belongs to the team
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipoId);
        assertThat(usuarios).hasSize(2);
        assertThat(usuarios.get(1).getEmail()).isEqualTo("user@ua");
    }

    @Test
    public void recuperarEquiposDeUsuario() {
        // GIVEN
        // Un usuario y dos equipos en la base de datos
        Map<String, Long> ids = addUsuariosEquiposBD();
        Long initialUserId = ids.get("initialUserId");
        Long equipoId = ids.get("equipoId");

        // WHEN
        // Recuperamos los equipos del usuario
        List<EquipoData> equipos = equipoService.equiposUsuario(initialUserId);

        // THEN
        // El usuario pertenece a los tres equipos
        assertThat(equipos).hasSize(3);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto 2");
        assertThat(equipos.get(2).getNombre()).isEqualTo("Proyecto 3");
    }

    @Test
    public void comprobarExcepciones() {
        // Comprobamos las excepciones lanzadas por los métodos
        // recuperarEquipo, añadirUsuarioAEquipo, usuariosEquipo y equiposUsuario
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);

        // Creamos un equipo pero no un usuario y comprobamos que también se lanza una excepción
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void borrarUsuarioDelEquipo() {
        // GIVEN
        // A new user and a team in the database
        Map<String, Long> ids = addUsuariosEquiposBD();
        Long initialUserId = ids.get("initialUserId");
        Long equipoId = ids.get("equipoId");

        // WHEN
        // Delete the user from the team
        equipoService.borrarUsuarioDelEquipo(equipoId, initialUserId);

        // THEN
        // Check that the user does not belong to the team
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipoId);
        assertThat(usuarios).hasSize(0);
    }

    @Test
    public void editarNombreDelEquipo() {
        Map<String, Long> ids = addUsuariosEquiposBD();
        Long equipoId = ids.get("equipoId");

        equipoService.modificarNombreEquipo(equipoId, "Proyecto 1 Modificado");

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        assertThat(equipo.getNombre()).isEqualTo("Proyecto 1 Modificado");
    }

}