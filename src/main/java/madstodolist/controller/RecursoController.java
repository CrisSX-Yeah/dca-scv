package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.RecursoNotFoundException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.RecursoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.RecursoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class RecursoController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    RecursoService recursoService;

    @Autowired
    ManagerUserSession managerUserSession;

    private void comprobarUsuarioLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null || !idUsuarioLogeado.equals(idUsuario)) {
            throw new UsuarioNoLogeadoException();
        }
    }

    @GetMapping("/usuarios/{id}/recursos/nuevo")
    public String formNuevoRecurso(@PathVariable(value="id") Long idUsuario,
                                   @ModelAttribute RecursoData recursoData, Model model,
                                   HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        return "formNuevoRecurso";
    }

    @PostMapping("/usuarios/{id}/recursos/nuevo")
    public String nuevoRecurso(@PathVariable(value="id") Long idUsuario, @ModelAttribute RecursoData recursoData,
                               Model model, RedirectAttributes flash,
                               HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        recursoService.nuevoRecursoUsuario(idUsuario, recursoData.getNombre());
        flash.addFlashAttribute("mensaje", "Recurso creado correctamente");
        return "redirect:/usuarios/" + idUsuario + "/recursos";
    }

    @GetMapping("/usuarios/{id}/recursos")
    public String listadoRecursos(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        List<RecursoData> recursos = recursoService.allRecursosUsuario(idUsuario);
        model.addAttribute("recursos", recursos);
        model.addAttribute("usuario", usuarioService.findById(idUsuario)); // Ensure UsuarioData is available
        return "listaRecursos";
    }

    @GetMapping("/recursos/{id}/editar")
    public String formEditaRecurso(@PathVariable(value="id") Long idRecurso, @ModelAttribute RecursoData recursoData,
                                   Model model, HttpSession session) {

        RecursoData recurso = recursoService.findById(idRecurso);
        if (recurso == null) {
            throw new RecursoNotFoundException();
        }

        comprobarUsuarioLogeado(recurso.getUsuarioId());

        UsuarioData usuario = usuarioService.findById(recurso.getUsuarioId());
        model.addAttribute("usuario", usuario);
        model.addAttribute("recurso", recurso);
        recursoData.setNombre(recurso.getNombre());
        return "formEditarRecurso";
    }

    @PostMapping("/recursos/{id}/editar")
    public String grabaRecursoModificado(@PathVariable(value="id") Long idRecurso, @ModelAttribute RecursoData recursoData,
                                         Model model, RedirectAttributes flash, HttpSession session) {
        RecursoData recurso = recursoService.findById(idRecurso);
        if (recurso == null) {
            throw new RecursoNotFoundException();
        }

        Long idUsuario = recurso.getUsuarioId();

        comprobarUsuarioLogeado(idUsuario);

        recursoService.modificaRecurso(idRecurso, recursoData.getNombre());
        flash.addFlashAttribute("mensaje", "Recurso modificado correctamente");
        return "redirect:/usuarios/" + recurso.getUsuarioId() + "/recursos";
    }

    // Method to handle resource deletion via POST
    @PostMapping("/recursos/{id}/borrar")
    public String borrarRecurso(@PathVariable(value="id") Long idRecurso, RedirectAttributes flash, Model model, HttpSession session) {
        RecursoData recurso = recursoService.findById(idRecurso);
        if (recurso == null) {
            throw new RecursoNotFoundException();
        }

        comprobarUsuarioLogeado(recurso.getUsuarioId());

        recursoService.borraRecurso(idRecurso);
        flash.addFlashAttribute("mensaje", "Recurso borrado correctamente");
        return "redirect:/usuarios/" + recurso.getUsuarioId() + "/recursos";
    }
}
