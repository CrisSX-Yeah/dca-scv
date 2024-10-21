package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.LoginData;
import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import madstodolist.service.UsuarioServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
// Import necessary annotations and classes
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginData", new LoginData());
        return "formLogin";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginData loginData, Model model, HttpSession session) {

        // Llamada al servicio para comprobar si el login es correcto
        UsuarioService.LoginStatus loginStatus = usuarioService.login(loginData.geteMail(), loginData.getPassword());

        if (loginStatus == UsuarioService.LoginStatus.LOGIN_OK) {
            UsuarioData usuario = usuarioService.findByEmail(loginData.geteMail());

            managerUserSession.logearUsuario(usuario.getId());

            model.addAttribute("usuario", usuario); // Add user to model
            return "redirect:/usuarios/" + usuario.getId() + "/tareas";
        } else if (loginStatus == UsuarioService.LoginStatus.USER_NOT_FOUND) {
            model.addAttribute("error", "No existe usuario");
            return "formLogin";
        } else if (loginStatus == UsuarioService.LoginStatus.ERROR_PASSWORD) {
            model.addAttribute("error", "Contraseña incorrecta");
            return "formLogin";
        } else if (loginStatus == UsuarioService.LoginStatus.USER_BLOCKED) {
            model.addAttribute("error", "Su cuenta ha sido bloqueada por el administrador");
            return "formLogin";
        }
        return "formLogin";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        RegistroData registroData = new RegistroData();
        model.addAttribute("registroData", registroData);

        // Check if an admin already exists
        boolean adminExists = usuarioService.existeAdministrador();
        model.addAttribute("adminExists", adminExists);

        return "formRegistro";
    }

    @PostMapping("/registro")
    public String registroSubmit(@Valid RegistroData registroData, BindingResult result, Model model) {

        if (result.hasErrors()) {
            // Set 'adminExists' when returning to the form due to validation errors
            boolean adminExists = usuarioService.existeAdministrador();
            model.addAttribute("adminExists", adminExists);
            return "formRegistro";
        }

        // Check if admin registration is allowed
        if (registroData.getAdmin() && usuarioService.existeAdministrador()) {
            model.addAttribute("error", "Ya existe un administrador en el sistema");
            boolean adminExists = usuarioService.existeAdministrador();
            model.addAttribute("adminExists", adminExists);
            return "formRegistro";
        }

        if (usuarioService.findByEmail(registroData.getEmail()) != null) {
            model.addAttribute("registroData", registroData);
            model.addAttribute("error", "El usuario " + registroData.getEmail() + " ya existe");
            boolean adminExists = usuarioService.existeAdministrador();
            model.addAttribute("adminExists", adminExists);
            return "formRegistro";
        }

        try {
            UsuarioData nuevoUsuario = usuarioService.registrar(registroData);

            // Redirect based on admin status
            if (nuevoUsuario.getAdmin()) {
                return "redirect:/login";
            } else {
                return "redirect:/login";
            }
        } catch (UsuarioServiceException e) {
            model.addAttribute("error", e.getMessage());
            boolean adminExists = usuarioService.existeAdministrador();
            model.addAttribute("adminExists", adminExists);
            return "formRegistro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        managerUserSession.logout();
        return "redirect:/login";
    }
}


