package madstodolist.controller;

import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.model.Equipo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import madstodolist.authentication.ManagerUserSession;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class EquipoController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    EquipoService equipoService;

    @Autowired
    ManagerUserSession managerUserSession;


    private void comprobarUsuarioLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null || !idUsuario.equals(idUsuarioLogeado)) {
            throw new UsuarioNoLogeadoException();
        }
    }

    @GetMapping("/logeados/equipos")
    public String listadoEquipos(Model model) {

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        model.addAttribute("equipos", equipos);

        return "listaEquipos";
    }



}
