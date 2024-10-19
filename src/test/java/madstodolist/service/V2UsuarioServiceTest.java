package madstodolist.service;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class V2UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private UsuarioData usuarioData;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");
        usuario.setNombre("Test User");
        usuario.setPassword("password");

        usuarioData = new UsuarioData();
        usuarioData.setId(1L);
        usuarioData.setEmail("test@example.com");
        usuarioData.setNombre("Test User");
        usuarioData.setPassword("password");
    }

    @Test
    public void loginShouldReturnLoginOkWhenCredentialsAreCorrect() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        UsuarioService.LoginStatus status = usuarioService.login("test@example.com", "password");
        assertEquals(UsuarioService.LoginStatus.LOGIN_OK, status);
    }

    @Test
    public void loginShouldReturnUserNotFoundWhenEmailDoesNotExist() {
        when(usuarioRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        UsuarioService.LoginStatus status = usuarioService.login("nonexistent@example.com", "password");
        assertEquals(UsuarioService.LoginStatus.USER_NOT_FOUND, status);
    }

    @Test
    public void loginShouldReturnErrorPasswordWhenPasswordIsIncorrect() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        UsuarioService.LoginStatus status = usuarioService.login("test@example.com", "wrongpassword");
        assertEquals(UsuarioService.LoginStatus.ERROR_PASSWORD, status);
    }

    @Test
    public void findByIdShouldReturnUsuarioDataWhenUserExists() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(modelMapper.map(usuario, UsuarioData.class)).thenReturn(usuarioData);

        UsuarioData result = usuarioService.findById(1L);
        assertNotNull(result);
        assertEquals("Test User", result.getNombre());
    }

    @Test
    public void findByIdShouldReturnNullWhenUserDoesNotExist() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        UsuarioData result = usuarioService.findById(2L);
        assertNull(result);
    }
}
