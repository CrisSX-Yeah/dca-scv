package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipoService {
    Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    EquipoRepository equipoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;


    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        Equipo equipo = new Equipo(nombre);
        equipoRepository.save(equipo);
        return modelMapper.map(equipo, EquipoData.class);
    }


    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null) {
            throw new EquipoServiceException("El equipo con id " + id + "no existe");
        }
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre() {
        logger.debug("Devolviendo todos los equipos ordenados alfabéticamente ");

        // Convert each Equipo to EquipoData and collect as a list
        List<EquipoData> equipos = equipoRepository.findAll()
                .stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());

        // Sort the list by the team name
        equipos.sort((a, b) -> a.getNombre().compareTo(b.getNombre()));

        return equipos;
    }


    @Transactional
    public void añadirUsuarioAEquipo(Long id, Long id1) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("El equipo con id " + id + "no existe");
        }

        Usuario usuario = usuarioRepository.findById(id1).orElse(null);

        if (usuario == null) {
            throw new EquipoServiceException("El usuario con id " + id + "no existe");
        }

        equipo.addUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("El equipo con id " + id + "no existe");
        }

        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null) {
            throw new EquipoServiceException("El usuario con id " + id + "no existe");
        }

        return  usuario.getEquipos().stream()
                .map(equipo -> modelMapper.map(equipo,EquipoData.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void borrarUsuarioDelEquipo(Long id, Long id1) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("El equipo con id " + id + "no existe");
        }

        Usuario usuario = usuarioRepository.findById(id1).orElse(null);

        if (usuario == null) {
            throw new EquipoServiceException("El usuario con id " + id + "no existe");
        }

        equipo.deleteUsuario(usuario);
    }
}
