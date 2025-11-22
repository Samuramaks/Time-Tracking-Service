package ru.samura.time_tracking_service.custom_exception;

/**
 * Исключение, выбрасываемое при операциях с записями времени,
 * когда у сотрудника отсутствуют смены (например, при попытке закрыть смену без предварительного открытия).
 * <p>
 * Типичный сценарий: вызов {@code clockOut()} для сотрудника, у которого нет ни одной записи в {@code time_entry}.
 * 
 * <p>
 * Обычно обрабатывается как {@code 400 Bad Request} или {@code 404 Not Found}, в зависимости от контекста.
 * 
 */
public class NoShiftFoundException extends RuntimeException {
    public NoShiftFoundException(String message) {
        super(message);
    }

    public NoShiftFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoShiftFoundException(Throwable cause) {
        super(cause);
    }
}