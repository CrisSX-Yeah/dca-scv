package madstodolist.repository;

import madstodolist.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String s);

    // New method to check if an admin exists
    boolean existsByAdminTrue();

    // New method to retrieve non-admin users
    Page<Usuario> findByAdminFalse(Pageable pageable);
}
