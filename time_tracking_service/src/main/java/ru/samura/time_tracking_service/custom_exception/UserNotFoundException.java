package ru.samura.time_tracking_service.custom_exception;

/**
 * Исключение, выбрасываемое при попытке доступа к несуществующему пользователю (сотруднику).
 * <p>
 * Возникает, например, при запросе сотрудника по неизвестному ID или при поиске по email, которого нет в системе.
 * 
 * <p>
 * Семантически соответствует HTTP-статусу {@code 404 Not Found}.
 * 
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
}