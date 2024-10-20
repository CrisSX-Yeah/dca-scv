package madstodolist.service;

import madstodolist.dto.RegistroData;
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

    // Helper method to create RegistroData DTO
    private RegistroData createRegistroData(String email, String password, String nombre, boolean admin) {
        RegistroData registroData = new RegistroData();
        registroData.setEmail(email);
        registroData.setPassword(password);
        registroData.setNombre(nombre);
        registroData.setAdmin(admin);
        return registroData;
    }

    @Test
    public void servicioLoginUsuario() {
        // GIVEN
        // Un usuario en la BD
        RegistroData registroData = createRegistroData("user@ua", "123", "Usuario Ejemplo", false);
        usuarioService.registrar(registroData);

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
        RegistroData registroData = createRegistroData("usuario.prueba2@gmail.com", "12345678", "Usuario Prueba", false);
        usuarioService.registrar(registroData);

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
        RegistroData registroData = createRegistroData("usuario.prueba@gmail.com", null, "Usuario Sin Password", false);

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(registroData);
        });
    }

    @Test
    public void servicioRegistroUsuarioExcepcionConEmailRepetido() {
        // GIVEN
        // Un usuario en la BD
        RegistroData registroData = createRegistroData("user@ua", "123", "Usuario Ejemplo", false);
        usuarioService.registrar(registroData);

        // THEN
        // Si registramos un usuario con un e-mail ya existente en la base de datos
        RegistroData registroDuplicado = createRegistroData("user@ua", "12345678", "Usuario Duplicado", false);

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(registroDuplicado);
        });
    }

    @Test
    public void servicioRegistroUsuarioDevuelveUsuarioConId() {
        // WHEN
        // Registramos un usuario con un e-mail no existente en la base de datos
        RegistroData registroData = createRegistroData("usuario.prueba@gmail.com", "12345678", "Usuario Prueba", false);
        UsuarioData usuarioNuevo = usuarioService.registrar(registroData);

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
        RegistroData registroData = createRegistroData("user@ua", "123", "Usuario Ejemplo", false);
        UsuarioData usuarioNuevo = usuarioService.registrar(registroData);
        Long usuarioId = usuarioNuevo.getId();

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
    public void registrarAdmin_WhenNoAdminExists_ShouldSucceed() {
        // GIVEN
        RegistroData registroData = new RegistroData();
        registroData.setEmail("admin@ua");
        registroData.setPassword("adminpass");
        registroData.setAdmin(true);

        // WHEN
        UsuarioData adminUser = usuarioService.registrar(registroData);

        // THEN
        assertThat(adminUser).isNotNull();
        assertThat(adminUser.getAdmin()).isTrue();
        assertThat(usuarioService.existeAdministrador()).isTrue();
    }

    @Test
    public void registrarAdmin_WhenAdminExists_ShouldFail() {
        // GIVEN
        // First, register an admin user
        RegistroData registroData1 = new RegistroData();
        registroData1.setEmail("admin@ua");
        registroData1.setPassword("adminpass");
        registroData1.setAdmin(true);
        usuarioService.registrar(registroData1);

        // Attempt to register another admin
        RegistroData registroData2 = new RegistroData();
        registroData2.setEmail("admin2@ua");
        registroData2.setPassword("adminpass2");
        registroData2.setAdmin(true);

        // WHEN & THEN
        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(registroData2);
        });
    }

    @Test
    public void registrarUser_WhenAdminExists_ShouldSucceed() {
        // GIVEN
        // Register an admin user
        RegistroData registroDataAdmin = new RegistroData();
        registroDataAdmin.setEmail("admin@ua");
        registroDataAdmin.setPassword("adminpass");
        registroDataAdmin.setAdmin(true);
        usuarioService.registrar(registroDataAdmin);

        // Register a regular user
        RegistroData registroDataUser = new RegistroData();
        registroDataUser.setEmail("user@ua");
        registroDataUser.setPassword("userpass");
        registroDataUser.setAdmin(false);

        // WHEN
        UsuarioData user = usuarioService.registrar(registroDataUser);

        // THEN
        assertThat(user).isNotNull();
        assertThat(user.getAdmin()).isFalse();
    }

    @Test
    public void listarUsuarios_ShouldReturnOnlyNonAdminUsers() {
        // GIVEN
        // Register an admin user
        RegistroData registroDataAdmin = new RegistroData();
        registroDataAdmin.setEmail("admin@ua");
        registroDataAdmin.setPassword("adminpass");
        registroDataAdmin.setAdmin(true);
        usuarioService.registrar(registroDataAdmin);

        // Register two regular users
        RegistroData registroDataUser1 = new RegistroData();
        registroDataUser1.setEmail("user1@ua");
        registroDataUser1.setPassword("userpass1");
        registroDataUser1.setAdmin(false);
        usuarioService.registrar(registroDataUser1);

        RegistroData registroDataUser2 = new RegistroData();
        registroDataUser2.setEmail("user2@ua");
        registroDataUser2.setPassword("userpass2");
        registroDataUser2.setAdmin(false);
        usuarioService.registrar(registroDataUser2);

        // WHEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<UsuarioData> usuariosPage = usuarioService.listarUsuarios(pageable);

        // THEN
        assertThat(usuariosPage.getTotalElements()).isEqualTo(2);
        assertThat(usuariosPage.getContent()).extracting("email")
                .containsExactlyInAnyOrder("user1@ua", "user2@ua");
    }

}
