package madstodolist.service;

import madstodolist.dto.RecursoData;
import madstodolist.model.Recurso;
import madstodolist.model.Usuario;
import madstodolist.repository.RecursoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// Import other necessary annotations and classes
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecursoService {

    Logger logger = LoggerFactory.getLogger(RecursoService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public RecursoData nuevoRecursoUsuario(Long idUsuario, String nombreRecurso) {
        logger.debug("Añadiendo recurso " + nombreRecurso + " al usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new RecursoServiceException("Usuario " + idUsuario + " no existe al crear recurso " + nombreRecurso);
        }
        Recurso recurso = new Recurso(usuario, nombreRecurso);
        recursoRepository.save(recurso);
        return modelMapper.map(recurso, RecursoData.class);
    }

    @Transactional(readOnly = true)
    public List<RecursoData> allRecursosUsuario(Long idUsuario) {
        logger.debug("Devolviendo todos los recursos del usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new RecursoServiceException("Usuario " + idUsuario + " no existe al listar recursos ");
        }
        // Map entities to DTOs
        List<RecursoData> recursos = usuario.getRecursos().stream()
                .map(recurso -> modelMapper.map(recurso, RecursoData.class))
                .collect(Collectors.toList());
        // Sort the list by recurso id
        Collections.sort(recursos, (a, b) -> a.getId() < b.getId() ? -1 : a.getId().equals(b.getId()) ? 0 : 1);
        return recursos;
    }

    @Transactional(readOnly = true)
    public RecursoData findById(Long recursoId) {
        logger.debug("Buscando recurso " + recursoId);
        Recurso recurso = recursoRepository.findById(recursoId).orElse(null);
        if (recurso == null) return null;
        else return modelMapper.map(recurso, RecursoData.class);
    }

    @Transactional
    public RecursoData modificaRecurso(Long idRecurso, String nuevoNombre) {
        logger.debug("Modificando recurso " + idRecurso + " - " + nuevoNombre);
        Recurso recurso = recursoRepository.findById(idRecurso).orElse(null);
        if (recurso == null) {
            throw new RecursoServiceException("No existe recurso con id " + idRecurso);
        }
        recurso.setNombre(nuevoNombre);
        recurso = recursoRepository.save(recurso);
        return modelMapper.map(recurso, RecursoData.class);
    }

    @Transactional
    public void borraRecurso(Long idRecurso) {
        logger.debug("Borrando recurso " + idRecurso);
        Recurso recurso = recursoRepository.findById(idRecurso).orElse(null);
        if (recurso == null) {
            throw new RecursoServiceException("No existe recurso con id " + idRecurso);
        }
        recursoRepository.delete(recurso);
    }

    @Transactional
    public boolean usuarioContieneRecurso(Long usuarioId, Long recursoId) {
        Recurso recurso = recursoRepository.findById(recursoId).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (recurso == null || usuario == null) {
            throw new RecursoServiceException("No existe recurso o usuario id");
        }
        return usuario.getRecursos().contains(recurso);
    }
}
