package madstodolist.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="Recurso no encontrado")
public class EquipoNotFoundException extends RuntimeException {
  public EquipoNotFoundException() {
    super("Recurso no encontrada");
  }
}
