package madstodolist.controller;

import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import madstodolist.authentication.ManagerUserSession;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        UsuarioData usuarioData = usuarioService.findById(idUsuarioLogeado);

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        List<Boolean> equiposPertenecientes = equipoService.listaEquiposPerteneceUsuario(idUsuarioLogeado);

        model.addAttribute("usuario", usuarioData);
        model.addAttribute("equipos", equipos);
        model.addAttribute("equiposPertenecientes", equiposPertenecientes);

        return "listaEquipos";
    }

    @GetMapping("/logeados/equipos/{equipo-id}/miembros")
    public String listadoMiembrosEquipo(@PathVariable(value="equipo-id") Long idEquipo,
                                        Model model) {

        List<UsuarioData> usuarios = equipoService.usuariosEquipo(idEquipo);

        model.addAttribute("usuarios", usuarios);

        return "listaUsuariosDeUnEquipo";
    }

    @GetMapping("/logeados/equipos/nuevo-equipo")
    public String formNuevoEquipo(@ModelAttribute("equipoData") EquipoData equipoData,
                                  Model model) {
        // You can perform any necessary initialization here if needed
        return "formNuevoEquipo";
    }

    @PostMapping("/logeados/equipos/nuevo-equipo")
    public String nuevoEquipo(@ModelAttribute EquipoData equipoData,
                              Model model, RedirectAttributes flash,
                              HttpSession session) {

        equipoService.crearEquipo(equipoData.getNombre());


        flash.addFlashAttribute("mensaje", "Equipo creado correctamente");


        return "redirect:/logeados/equipos";
    }

    @PostMapping("/logeados/equipos/{equipo-id}/agrega-usuario-logeado/{usuario-id}")
    public String agregarUsuarioLogeadoEnEquipo(@PathVariable(value="equipo-id") Long idEquipo,
                                                @PathVariable(value="usuario-id") Long idUsuario,
                                                @ModelAttribute EquipoData equipoData,
                                                Model model, RedirectAttributes flash,
                                                HttpSession session) {
        equipoService.añadirUsuarioAEquipo(idEquipo, idUsuario);
        return "redirect:/logeados/equipos";
    }

    @PostMapping("/logeados/equipos/{equipo-id}/elimina-usuario-logeado/{usuario-id}")
    public String eliminaUsuarioLogeadoDeEquipo(@PathVariable(value="equipo-id") Long idEquipo,
                                                @PathVariable(value="usuario-id") Long idUsuario,
                                                @ModelAttribute EquipoData equipoData,
                                                Model model, RedirectAttributes flash,
                                                HttpSession session) {
        equipoService.borrarUsuarioDelEquipo(idEquipo, idUsuario);
        return "redirect:/logeados/equipos";
    }

    @GetMapping("/admin/auth/equipos/{id}/editar")
    public String formEditarEquipo(@ModelAttribute("equipoData") EquipoData equipoData,
                                  Model model) {
        EquipoData equipo = equipoService.recuperarEquipo(equipoData.getId());
        model.addAttribute("equipo", equipo);
        return "formEditarEquipo";
    }

    @PostMapping("/admin/auth/equipos/{id}/editar")
    public String editarEquipo(@ModelAttribute EquipoData equipoData,
                              Model model, RedirectAttributes flash,
                              HttpSession session) {

        equipoService.modificarNombreEquipo(equipoData.getId(), equipoData.getNombre());


        flash.addFlashAttribute("mensaje", "Equipo Modificado correctamente");

        return "redirect:/logeados/equipos";
    }

}
