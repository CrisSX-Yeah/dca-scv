package madstodolist.interceptor;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UnauthorizedAccessException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor to ensure that only admin users can access certain URLs.
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = managerUserSession.usuarioLogeado();
        if (userId == null) {
            // User not logged in
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(userId);
        if (usuario == null || !usuario.getAdmin()) {
            // User is not admin
            throw new UnauthorizedAccessException("Permiso insuficiente para acceder a esta página.");
        }

        // User is admin, allow access
        return true;
    }

}
