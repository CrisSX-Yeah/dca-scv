package madstodolist.service;

import madstodolist.dto.RegistroData;
import madstodolist.dto.TareaData;
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
public class TareaServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    // Método para inicializar los datos de prueba en la BD
    Map<String, Long> addUsuarioTareasBD() {
        // Añadimos un usuario a la base de datos
        RegistroData registroData = new RegistroData();
        registroData.setEmail("user@ua");
        registroData.setPassword("123");
        UsuarioData usuarioNuevo = usuarioService.registrar(registroData);

        // Y añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Lavar coche");
        TareaData tarea2 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Renovar DNI");
        TareaData tarea3 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Comprar pan");

        Long tareaId1 = tarea1.getId();
        Long tareaId2 = tarea2.getId();
        Long tareaId3 = tarea3.getId();

        Long usuarioId = usuarioNuevo.getId();

        tareaService.asignarHoras(tareaId1, 4);
        tareaService.asignarHoras(tareaId2, 6);
        tareaService.asignarHoras(tareaId3, 5);

        tareaService.calcularPromedioHoras(usuarioId);

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("tareaId", tareaId1);
        ids.put("tareaId2", tareaId2);
        ids.put("tareaId3", tareaId3);
        return ids;
    }

    @Test
    public void testNuevaTareaUsuario() {
        // GIVEN
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // WHEN
        TareaData nuevaTarea = tareaService.nuevaTareaUsuario(usuarioId, "Práctica 1 de MADS");

        // THEN
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).hasSize(4);
        assertThat(tareas).contains(nuevaTarea);
    }

    @Test
    public void testBuscarTarea() {
        Long tareaId = addUsuarioTareasBD().get("tareaId");
        TareaData lavarCoche = tareaService.findById(tareaId);
        assertThat(lavarCoche).isNotNull();
        assertThat(lavarCoche.getTitulo()).isEqualTo("Lavar coche");
    }

    @Test
    public void testModificarTarea() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        tareaService.modificaTarea(tareaId, "Limpiar los cristales del coche");

        TareaData tareaBD = tareaService.findById(tareaId);
        assertThat(tareaBD.getTitulo()).isEqualTo("Limpiar los cristales del coche");

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).contains(tareaBD);
    }

    @Test
    public void testBorrarTarea() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        tareaService.borraTarea(tareaId);

        assertThat(tareaService.findById(tareaId)).isNull();

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).hasSize(2);
    }

    @Test
    public void asignarEtiquetaATarea() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        assertThat(tareaService.usuarioContieneTarea(usuarioId, tareaId)).isTrue();
    }



    //A partir de aquí los tests son añadidos por mí para probar la funcionalidad de las horas de las tareas



    @Test
    public void testAsignarHorasTareas() {
        // GIVEN
        // Un usuario y una tarea en la BD

        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");
        Long tareaId2 = ids.get("tareaId2");
        Long tareaId3 = ids.get("tareaId3");

        // WHEN & THEN
        // al buscar por el identificador en la base de datos se devuelven las tarea modificadas

        TareaData tareaBD = tareaService.findById(tareaId);
        TareaData tareaBD2 = tareaService.findById(tareaId2);
        TareaData tareaBD3 = tareaService.findById(tareaId3);

        assertThat(tareaBD.getHours()).isEqualTo(4);
        assertThat(tareaBD2.getHours()).isEqualTo(6);
        assertThat(tareaBD3.getHours()).isEqualTo(5);

        // y el usuario tiene también esas tareas modificada.
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        assertThat(tareas).contains(tareaBD);
        assertThat(tareas).contains(tareaBD2);
        assertThat(tareas).contains(tareaBD3);

    }

    @Test
    public void testIncrementarHorasTarea() {
        // GIVEN
        // Un usuario y una tarea en la BD

        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // WHEN
        //Incrementamos la hora de la tarea
        tareaService.incrementarHoras(tareaId);

        //THEN Las horas de la atarea habrán sido incrementadas en 1
        TareaData tareaBD = tareaService.findById(tareaId);
        assertThat(tareaBD.getHours()).isEqualTo(5);

        // y el usuario tiene también esa tarea modificada.
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).contains(tareaBD);

    }

    @Test
    public void testCalcularPromedioHorasTarea() {
        // Añadimos un usuario a la base de datos
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");

        UsuarioData usuarioDB = usuarioService.findById(usuarioId);

        assertThat(usuarioDB.getPromedioTareas()).isEqualTo(5);

    }
}
