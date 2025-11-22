package ru.samura.time_tracking_service.custom_exception;

/**
 * Исключение, выбрасываемое при попытке закрыть смену,
 * которая уже была завершена сегодня.
 * <p>
 * Типичный сценарий: сотрудник дважды нажал «Уход» в течение одного дня.
 * 
 * <p>
 * Указывает на дублирующий запрос или ошибку в клиентской логике.
 * Обычно обрабатывается как {@code 409 Conflict} или {@code 400 Bad Request}.
 * 
 */
public class ClosedShiftTodayException extends RuntimeException {
    public ClosedShiftTodayException(String message) {
        super(message);
    }

    public ClosedShiftTodayException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClosedShiftTodayException(Throwable cause) {
        super(cause);
    }
}