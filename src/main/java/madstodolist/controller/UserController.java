package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
// Import necessary Spring MVC annotations and classes
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registrados")
    public String listarUsuarios(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UsuarioData> usuariosPage = usuarioService.listarUsuarios(pageable);
        model.addAttribute("usuariosPage", usuariosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosPage.getTotalPages());
        model.addAttribute("totalElements", usuariosPage.getTotalElements());
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable Long id, Model model) {
        UsuarioData usuario = usuarioService.findById(id);
        if (usuario == null) {
            // Option 1: Redirect to a 404 error page
            return "error/404"; // Ensure you have a 404.html in src/main/resources/templates/error/

            // Option 2: Redirect to the user list with an error message
            // model.addAttribute("errorMessage", "Usuario no encontrado.");
            // return "redirect:/registrados";
        }
        model.addAttribute("usuario", usuario);
        return "usuarioDescripcion";
    }

}
