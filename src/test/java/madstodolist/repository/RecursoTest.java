package madstodolist.repository;

import madstodolist.model.Recurso;
import madstodolist.model.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class RecursoTest {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    RecursoRepository recursoRepository;

    //
    // Tests modelo Recurso en memoria, sin la conexión con la BD
    //

    @Test
    public void crearRecurso() {
        // GIVEN
        // Un usuario nuevo creado en memoria, sin conexión con la BD,
        Usuario usuario = new Usuario("ana.perez@gmail.com");

        // WHEN
        // se crea un nuevo recurso con ese usuario,
        Recurso recurso = new Recurso(usuario, "Libro de Java");

        // THEN
        // el nombre y el usuario del recurso son los correctos.
        assertThat(recurso.getNombre()).isEqualTo("Libro de Java");
        assertThat(recurso.getUsuario()).isEqualTo(usuario);
    }

    @Test
    public void laListaDeRecursosDeUnUsuarioSeActualizaEnMemoriaConUnNuevoRecurso() {
        // GIVEN
        // Un usuario nuevo creado en memoria, sin conexión con la BD,
        Usuario usuario = new Usuario("ana.perez@gmail.com");

        // WHEN
        // se crea un recurso de ese usuario,
        Set<Recurso> recursos = usuario.getRecursos();
        Recurso recurso = new Recurso(usuario, "Libro de Java");

        // THEN
        // el recurso creado se ha añadido a la lista de recursos del usuario.
        assertThat(usuario.getRecursos()).contains(recurso);
        assertThat(recursos).contains(recurso);
    }

    @Test
    public void comprobarIgualdadRecursosSinId() {
        // GIVEN
        // Creados tres recursos sin identificador, y dos de ellas con
        // la misma descripción
        Usuario usuario = new Usuario("ana.perez@gmail.com");
        Recurso recurso1 = new Recurso(usuario, "Libro de Java");
        Recurso recurso2 = new Recurso(usuario, "Libro de Java");
        Recurso recurso3 = new Recurso(usuario, "Curso de Spring");

        // THEN
        // son iguales (Equal) los recursos que tienen la misma descripción.
        assertThat(recurso1).isEqualTo(recurso2);
        assertThat(recurso1).isNotEqualTo(recurso3);
    }

    @Test
    public void comprobarIgualdadRecursosConId() {
        // GIVEN
        // Creados tres recursos con distintas descripciones y dos de ellas
        // con el mismo identificador,
        Usuario usuario = new Usuario("ana.perez@gmail.com");
        Recurso recurso1 = new Recurso(usuario, "Libro de Java");
        Recurso recurso2 = new Recurso(usuario, "Curso de Spring");
        Recurso recurso3 = new Recurso(usuario, "Libro de Java");
        recurso1.setId(1L);
        recurso2.setId(2L);
        recurso3.setId(1L);

        // THEN
        // son iguales (Equal) los recursos que tienen el mismo identificador.
        assertThat(recurso1).isEqualTo(recurso3);
        assertThat(recurso1).isNotEqualTo(recurso2);
    }

    //
    // Tests RecursoRepository.
    // El código que trabaja con repositorios debe
    // estar en un entorno transactional, para que todas las peticiones
    // estén en la misma conexión a la base de datos, las entidades estén
    // conectadas y sea posible acceder a colecciones LAZY.
    //

    @Test
    @Transactional
    public void guardarRecursoEnBaseDatos() {
        // GIVEN
        // Un usuario en la base de datos.
        Usuario usuario = new Usuario("usuario@ejemplo.com");
        usuarioRepository.save(usuario);

        Recurso recurso = new Recurso(usuario, "Libro de Java");

        // WHEN
        // salvamos el recurso en la BD,
        recursoRepository.save(recurso);

        // THEN
        // se actualiza el id del recurso,
        assertThat(recurso.getId()).isNotNull();

        // y con ese identificador se recupera de la base de datos el recurso
        // con los valores correctos de las propiedades y la relación con
        // el usuario actualizado también correctamente.
        Recurso recursoBD = recursoRepository.findById(recurso.getId()).orElse(null);
        assertThat(recursoBD.getNombre()).isEqualTo(recurso.getNombre());
        assertThat(recursoBD.getUsuario()).isEqualTo(usuario);
    }

    @Test
    @Transactional
    public void salvarRecursoEnBaseDatosConUsuarioNoBDLanzaExcepcion() {
        // GIVEN
        // Un usuario nuevo que no está en la BD
        // y un recurso asociado a ese usuario,
        Usuario usuario = new Usuario("ana.perez@gmail.com");
        Recurso recurso = new Recurso(usuario, "Libro de Java");

        // WHEN // THEN
        // se lanza una excepción al intentar salvar el recurso en la BD
        Assertions.assertThrows(Exception.class, () -> {
            recursoRepository.save(recurso);
        });
    }

    @Test
    @Transactional
    public void unUsuarioTieneUnaListaDeRecursos() {
        // GIVEN
        // Un usuario con 2 recursos en la base de datos
        Usuario usuario = new Usuario("usuario@ejemplo.com");
        usuarioRepository.save(usuario);
        Long usuarioId = usuario.getId();

        Recurso recurso1 = new Recurso(usuario, "Libro de Java");
        Recurso recurso2 = new Recurso(usuario, "Curso de Spring");
        recursoRepository.save(recurso1);
        recursoRepository.save(recurso2);

        // WHEN
        // recuperamos el usuario de la base de datos,

        Usuario usuarioRecuperado = usuarioRepository.findById(usuarioId).orElse(null);

        // THEN
        // su lista de recursos también se recupera, porque se ha
        // definido la relación de usuario y recursos como EAGER.
        assertThat(usuarioRecuperado.getRecursos()).hasSize(2);
    }

    @Test
    @Transactional
    public void añadirUnRecursoAUnUsuarioEnBD() {
        // GIVEN
        // Un usuario en la base de datos
        Usuario usuario = new Usuario("usuario@ejemplo.com");
        usuarioRepository.save(usuario);
        Long usuarioId = usuario.getId();

        // WHEN
        // Creamos un nuevo recurso con el usuario recuperado de la BD
        // y lo salvamos,
        Usuario usuarioBD = usuarioRepository.findById(usuarioId).orElse(null);
        Recurso recurso = new Recurso(usuarioBD, "Libro de Java");
        recursoRepository.save(recurso);
        Long recursoId = recurso.getId();

        // THEN
        // el recurso queda guardado en la BD asociado al usuario
        Recurso recursoBD = recursoRepository.findById(recursoId).orElse(null);
        assertThat(recursoBD).isEqualTo(recurso);
        assertThat(recurso.getUsuario()).isEqualTo(usuarioBD);

        // y si recuperamos el usuario se obtiene el nuevo recurso
        usuarioBD = usuarioRepository.findById(usuarioId).orElse(null);
        assertThat(usuarioBD.getRecursos()).contains(recursoBD);
    }

    @Test
    @Transactional
    public void cambioEnLaEntidadEnTransactionalModificaLaBD() {
        // GIVEN
        // Un usuario y un recurso en la base de datos
        Usuario usuario = new Usuario("usuario@ejemplo.com");
        usuarioRepository.save(usuario);
        Recurso recurso = new Recurso(usuario, "Libro de Java");
        recursoRepository.save(recurso);

        // Recuperamos el recurso
        Long recursoId = recurso.getId();
        recurso = recursoRepository.findById(recursoId).orElse(null);

        // WHEN
        // modificamos el nombre del recurso
        recurso.setNombre("Libro de Spring");

        // THEN
        // el nombre queda actualizado en la BD.
        Recurso recursoBD = recursoRepository.findById(recursoId).orElse(null);
        assertThat(recursoBD.getNombre()).isEqualTo(recurso.getNombre());
    }
}
