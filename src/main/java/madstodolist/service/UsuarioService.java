package madstodolist.service;

import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public enum LoginStatus {LOGIN_OK, USER_NOT_FOUND, ERROR_PASSWORD}

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public LoginStatus login(String eMail, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
        if (!usuario.isPresent()) {
            return LoginStatus.USER_NOT_FOUND;
        } else if (!usuario.get().getPassword().equals(password)) {
            return LoginStatus.ERROR_PASSWORD;
        } else {
            return LoginStatus.LOGIN_OK;
        }
    }

    // Se añade un usuario en la aplicación.
    // El email y password del usuario deben ser distinto de null
    // El email no debe estar registrado en la base de datos
    @Transactional
    public UsuarioData registrar(RegistroData registroData) {
        Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(registroData.getEmail());
        if (usuarioBD.isPresent())
            throw new UsuarioServiceException("El usuario " + registroData.getEmail() + " ya está registrado");
        else if (registroData.getEmail() == null)
            throw new UsuarioServiceException("El usuario no tiene email");
        else if (registroData.getPassword() == null)
            throw new UsuarioServiceException("El usuario no tiene password");
        else {
            Usuario usuarioNuevo = modelMapper.map(registroData, Usuario.class);

            // Check if setting admin is allowed
            if (registroData.getAdmin()) {
                if (existeAdministrador()) {
                    throw new UsuarioServiceException("Ya existe un administrador en el sistema");
                } else {
                    usuarioNuevo.setAdmin(true);
                }
            } else {
                usuarioNuevo.setAdmin(false);
            }

            usuarioNuevo = usuarioRepository.save(usuarioNuevo);
            return modelMapper.map(usuarioNuevo, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public boolean existeAdministrador() {
        return usuarioRepository.existsByAdminTrue();
    }

    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    // New method to retrieve paginated list of users
    @Transactional(readOnly = true)
    public Page<UsuarioData> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findByAdminFalse(pageable)
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class));
    }
}



