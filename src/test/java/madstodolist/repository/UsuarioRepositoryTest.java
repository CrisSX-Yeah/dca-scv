package madstodolist.repository;

import madstodolist.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Find by email should return user when email exists")
    public void findByEmail_ShouldReturnUser_WhenEmailExists() {
        Usuario usuario = new Usuario("user@example.com");
        usuario.setNombre("Test User");
        usuario.setPassword("password");
        usuarioRepository.save(usuario);

        Optional<Usuario> found = usuarioRepository.findByEmail("user@example.com");
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getNombre());
    }

    @Test
    @DisplayName("Find by email should return empty when email does not exist")
    public void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        Optional<Usuario> found = usuarioRepository.findByEmail("nonexistent@example.com");
        assertFalse(found.isPresent());
    }
}
