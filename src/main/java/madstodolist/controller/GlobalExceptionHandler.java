package madstodolist.controller;

import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNoLogeadoException.class)
    public ModelAndView handleUsuarioNoLogeadoException(UsuarioNoLogeadoException ex) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:/login?error=not_logged_in");
        return mav;
    }

    @ExceptionHandler(TareaNotFoundException.class)
    public ModelAndView handleTareaNotFoundException(TareaNotFoundException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", ex.getMessage());
        mav.setViewName("error/404"); // You can create a custom 404 page
        return mav;
    }


    // Optionally, handle other exceptions
}
