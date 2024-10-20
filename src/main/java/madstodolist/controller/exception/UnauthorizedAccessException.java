package madstodolist.controller.exception;

/**
 * Exception thrown when a user tries to access a resource without sufficient permissions.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
