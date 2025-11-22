package ru.samura.time_tracking_service.custom_exception;

/**
 * Исключение, выбрасываемое при попытке открыть новую смену,
 * когда у сотрудника уже есть активная (не завершённая) смена в текущий день.
 * <p>
 * Типичный сценарий: сотрудник дважды нажал «Приход» в течение одного дня.
 * 
 * <p>
 * Обычно обрабатывается на уровне контроллера как {@code 409 Conflict} или {@code 400 Bad Request}.
 * 
 */
public class ActiveShiftTodayException extends RuntimeException {
    public ActiveShiftTodayException(String message) {
        super(message);
    }

    public ActiveShiftTodayException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActiveShiftTodayException(Throwable cause) {
        super(cause);
    }
}