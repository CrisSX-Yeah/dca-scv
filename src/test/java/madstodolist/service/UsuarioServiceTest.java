package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    // Método para inicializar los datos de prueba en la BD con parámetros
    Long addUsuarioBD(String email, String nombre) {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail(email);
        usuario.setNombre(nombre);
        usuario.setPassword("123");
        UsuarioData nuevoUsuario = usuarioService.registrar(usuario);
        return nuevoUsuario.getId();
    }

    // Manteniendo el método anterior por compatibilidad con otras pruebas
    Long addUsuarioBD() {
        return addUsuarioBD("user@ua", "Usuario Ejemplo");
    }

    @Test
    public void servicioLoginUsuario() {
        // GIVEN
        // Un usuario en la BD
        addUsuarioBD();

        // WHEN
        // intentamos logear un usuario y contraseña correctos
        UsuarioService.LoginStatus loginStatus1 = usuarioService.login("user@ua", "123");

        // intentamos logear un usuario correcto, con una contraseña incorrecta
        UsuarioService.LoginStatus loginStatus2 = usuarioService.login("user@ua", "000");

        // intentamos logear un usuario que no existe
        UsuarioService.LoginStatus loginStatus3 = usuarioService.login("pepito.perez@gmail.com", "12345678");

        // THEN
        // el valor devuelto por el primer login es LOGIN_OK,
        assertThat(loginStatus1).isEqualTo(UsuarioService.LoginStatus.LOGIN_OK);

        // el valor devuelto por el segundo login es ERROR_PASSWORD,
        assertThat(loginStatus2).isEqualTo(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // y el valor devuelto por el tercer login es USER_NOT_FOUND.
        assertThat(loginStatus3).isEqualTo(UsuarioService.LoginStatus.USER_NOT_FOUND);
    }

    @Test
    public void servicioRegistroUsuario() {
        // WHEN
        // Registramos un usuario con un e-mail no existente en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba2@gmail.com");
        usuario.setPassword("12345678");

        usuarioService.registrar(usuario);

        // THEN
        // el usuario se añade correctamente al sistema
        UsuarioData usuarioBaseDatos = usuarioService.findByEmail("usuario.prueba2@gmail.com");
        assertThat(usuarioBaseDatos).isNotNull();
        assertThat(usuarioBaseDatos.getEmail()).isEqualTo("usuario.prueba2@gmail.com");
    }

    @Test
    public void servicioRegistroUsuarioExcepcionConNullPassword() {
        // WHEN, THEN
        // Si intentamos registrar un usuario con un password null
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }

    @Test
    public void servicioRegistroUsuarioExcepcionConEmailRepetido() {
        // GIVEN
        // Un usuario en la BD
        addUsuarioBD();

        // THEN
        // Si registramos un usuario con un e-mail ya existente en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("12345678");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }

    @Test
    public void servicioRegistroUsuarioDevuelveUsuarioConId() {
        // WHEN
        // Registramos un usuario con un e-mail no existente en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");
        usuario.setPassword("12345678");

        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // THEN
        // se actualiza el identificador del usuario
        assertThat(usuarioNuevo.getId()).isNotNull();

        // con el identificador que se ha guardado en la BD
        UsuarioData usuarioBD = usuarioService.findById(usuarioNuevo.getId());
        assertThat(usuarioBD).isEqualTo(usuarioNuevo);
    }

    @Test
    public void servicioConsultaUsuarioDevuelveUsuario() {
        // GIVEN
        // Un usuario en la BD
        Long usuarioId = addUsuarioBD();

        // WHEN
        // recuperamos un usuario usando su e-mail
        UsuarioData usuario = usuarioService.findByEmail("user@ua");

        // THEN
        // el usuario obtenido es el correcto
        assertThat(usuario.getId()).isEqualTo(usuarioId);
        assertThat(usuario.getEmail()).isEqualTo("user@ua");
        assertThat(usuario.getNombre()).isEqualTo("Usuario Ejemplo");
    }

    @Test
    public void servicioListarUsuariosPaginado() {
        // GIVEN
        addUsuarioBD("user1@ua", "Usuario Uno");
        addUsuarioBD("user2@ua", "Usuario Dos");
        addUsuarioBD("user3@ua", "Usuario Tres");

        // WHEN
        Pageable pageable = PageRequest.of(0, 2); // Page 0 with 2 users per page
        Page<UsuarioData> usuariosPage = usuarioService.listarUsuarios(pageable);

        // THEN
        assertThat(usuariosPage.getTotalElements()).isEqualTo(3); // Total 3 users in the system
        assertThat(usuariosPage.getTotalPages()).isEqualTo(2); // 2 pages (3 users with page size 2)
        assertThat(usuariosPage.getContent().size()).isEqualTo(2); // 2 users in the first page

        // Check that the first page contains the correct users
        UsuarioData usuario1 = usuariosPage.getContent().get(0);
        UsuarioData usuario2 = usuariosPage.getContent().get(1);

        assertThat(usuario1.getEmail()).isEqualTo("user1@ua");
        assertThat(usuario2.getEmail()).isEqualTo("user2@ua");

        // Check second page content
        Pageable secondPage = PageRequest.of(1, 2); // Page 1 with 2 users per page
        Page<UsuarioData> secondUsuariosPage = usuarioService.listarUsuarios(secondPage);
        assertThat(secondUsuariosPage.getContent().size()).isEqualTo(1); // 1 user in the second page

        UsuarioData usuario3 = secondUsuariosPage.getContent().get(0);
        assertThat(usuario3.getEmail()).isEqualTo("user3@ua");
    }
}
