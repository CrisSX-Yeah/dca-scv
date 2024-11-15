package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class TareaController {


    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    ManagerUserSession managerUserSession;

    private void comprobarUsuarioLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null || !idUsuario.equals(idUsuarioLogeado)) {
            throw new UsuarioNoLogeadoException();
        }
    }

    @GetMapping("/usuarios/{id}/tareas")
    public String listadoTareas(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {

        //UsuarioData usuario = usuarioService.findById(idUsuario);

        comprobarUsuarioLogeado(idUsuario);

        List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);
        //tareaService.calcularPromedioHoras(idUsuario);

        //model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);

        return "listaTareas";
    }


    @GetMapping("/usuarios/{id}/tareas/nueva")
    public String formNuevaTarea(@PathVariable(value="id") Long idUsuario,
                                 @ModelAttribute TareaData tareaData, Model model,
                                 HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        return "formNuevaTarea";
    }

    @PostMapping("/usuarios/{id}/tareas/nueva")
    public String nuevaTarea(@PathVariable(value="id") Long idUsuario, @ModelAttribute TareaData tareaData,
                             Model model, RedirectAttributes flash,
                             HttpSession session) {

        UsuarioData usuario = usuarioService.findById(idUsuario);
        comprobarUsuarioLogeado(idUsuario);

        tareaService.nuevaTareaUsuario(idUsuario, tareaData.getTitulo());
        tareaService.calcularPromedioHoras(idUsuario);

        flash.addFlashAttribute("mensaje", "Tarea creada correctamente");
        model.addAttribute("usuario", usuario);

        return "redirect:/usuarios/" + idUsuario + "/tareas";
    }

    @GetMapping("/tareas/{id}/editar")
    public String formEditaTarea(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                 Model model, HttpSession session) {

        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        UsuarioData usuario = usuarioService.findById(tarea.getUsuarioId());
        model.addAttribute("usuario", usuario);
        model.addAttribute("tarea", tarea);
        tareaData.setTitulo(tarea.getTitulo());
        return "formEditarTarea";
    }

    @GetMapping("/tasks-auth")
    public String handleTasksLink(RedirectAttributes redirectAttributes) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (idUsuarioLogeado == null) {
            // User is not logged in, redirect to login page
            return "redirect:/login";
        } else {
            // User is logged in, redirect to the tasks page for the user
            return "redirect:/usuarios/" + idUsuarioLogeado + "/tareas";
        }
    }

    @PostMapping("/tareas/{id}/editar")
    public String grabaTareaModificada(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                       Model model, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuario = tarea.getUsuarioId();

        comprobarUsuarioLogeado(idUsuario);

        tareaService.modificaTarea(idTarea, tareaData.getTitulo());
        flash.addFlashAttribute("mensaje", "Tarea modificada correctamente");
        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    // New method to handle task deletion via POST
    @PostMapping("/tareas/{id}/borrar")
    public String borrarTarea(@PathVariable(value="id") Long idTarea, RedirectAttributes flash, Model model, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        tareaService.borraTarea(idTarea);
        flash.addFlashAttribute("mensaje", "Tarea borrada correctamente");
        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    @GetMapping("/recursos-auth")
    public String handleRecursosLink(RedirectAttributes redirectAttributes) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (idUsuarioLogeado == null) {
            // User is not logged in, redirect to login page
            return "redirect:/login";
        } else {
            // User is logged in, redirect to the recursos page for the user
            return "redirect:/usuarios/" + idUsuarioLogeado + "/recursos";
        }
    }

    @GetMapping("/tareas/{id}/asignarHoras")
    public String formAsignaHorasTarea(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                 Model model, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        UsuarioData usuario = usuarioService.findById(tarea.getUsuarioId());
        model.addAttribute("usuario", usuario);
        model.addAttribute("tarea", tarea);
        tareaData.setTitulo(tarea.getTitulo());
        return "asignarHorasTarea";
    }

    @PostMapping("/tareas/{id}/asignarHoras")
    public String asignarHorasTarea(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                       Model model, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuario = tarea.getUsuarioId();
        UsuarioData usuario = usuarioService.findById(idUsuario);

        comprobarUsuarioLogeado(idUsuario);

        tareaService.asignarHoras(idTarea, tareaData.getHours());
        tareaService.calcularPromedioHoras(idUsuario);

        flash.addFlashAttribute("mensaje", "Se asignaron con éxito las horas a la Tarea");
        model.addAttribute("usuario", usuario);

        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    @PostMapping("/tareas/{id}/incrementarHoras")
    public String incrementarHorasTarea(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                    Model model, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuario = tarea.getUsuarioId();
        UsuarioData usuario = usuarioService.findById(idUsuario);

        comprobarUsuarioLogeado(idUsuario);

        tarea.setHours(0);
        usuario.setPromedioTareas(0);

        flash.addFlashAttribute("mensaje", "Se han incrementado las horas de la tarea con éxito");
        model.addAttribute("usuario", usuario);

        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }


    // Old method to handle task deletion via GET
    /*
    @DeleteMapping("/tareas/{id}")
    @ResponseBody
    public String borrarTarea(@PathVariable(value="id") Long idTarea, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        tareaService.borraTarea(idTarea);
        return "";
    }
     */
}
