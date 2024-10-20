package madstodolist.repository;

import madstodolist.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String s);

    // New method to check if an admin exists
    boolean existsByAdminTrue();
}
