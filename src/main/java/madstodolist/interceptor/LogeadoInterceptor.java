package madstodolist.interceptor;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor to ensure that only logged-in users can access certain URLs.
 */
@Component
public class LogeadoInterceptor implements HandlerInterceptor {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = managerUserSession.usuarioLogeado();

        if (userId == null) {
            // User not logged in, throw an exception or redirect to login
            throw new NotFoundException("Página no encontrada."); // O redirige a login si prefieres
            // response.sendRedirect("/login?error=not_logged_in");
            // return false;
        }

        // User is logged in, allow access
        return true;
    }
}
