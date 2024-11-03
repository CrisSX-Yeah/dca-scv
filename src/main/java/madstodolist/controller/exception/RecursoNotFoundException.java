package madstodolist.controller.exception;

public class RecursoNotFoundException extends NotFoundException {
    public RecursoNotFoundException() {
        super("Recurso no encontrado");
    }

    public RecursoNotFoundException(String message) {
        super(message);
    }
}
