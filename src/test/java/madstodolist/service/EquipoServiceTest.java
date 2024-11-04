package madstodolist.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import madstodolist.dto.EquipoData;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

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
}