package ru.samura.time_tracking_service.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ответ на операцию начала смены (clock-in).
 * <p>
 * Содержит информацию о созданной записи времени и контекстную подсказку
 * для клиента (например, UI может показать предупреждение, если смена уже была начата сегодня).
 * 
 *
 * @param timeEntryId      идентификатор новой записи времени ({@link ru.samura.time_tracking_service.entity.TimeEntry#id})
 * @param employeeId       идентификатор сотрудника, для которого открыта смена
 * @param checkIn          точное время начала смены (момент вызова clock-in)
 * @param isFirstShiftToday {@code false}, если у сотрудника уже была активная смена сегодня
 *                         (например, прерванная или не закрытая); может использоваться для отображения предупреждения.
 *                         ⚠️ Примечание: в текущей реализации сервиса такая ситуация приводит к исключению,
 *                         поэтому в нормальном потоке это значение всегда {@code true}.
 */
public record ClockInResponse(
    UUID timeEntryId,         // ID созданной смены
    UUID employeeId,          // за кого
    LocalDateTime checkIn,    // когда вошёл
    boolean isFirstShiftToday // предупреждение: "Вы уже начинали смену сегодня"
) {}