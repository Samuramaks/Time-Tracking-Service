package ru.samura.time_tracking_service.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ответ на операцию окончания смены (clock-out).
 * <p>
 * Содержит информацию о завершённой записи времени и флаг, указывающий,
 * была ли смена сегодня уже закрыта ранее.
 * 
 *
 * @param timeEntryId               идентификатор обновлённой записи времени
 * @param employeeId                идентификатор сотрудника, чья смена закрыта
 * @param checkOut                  точное время окончания смены (момент вызова clock-out)
 * @param isFirstShiftTodayIsOver   {@code false}, если смена сегодня уже была закрыта ранее
 *                                  (например, при повторной попытке завершения);
 *                                  в текущей реализации сервиса такая ситуация приводит к исключению,
 *                                  поэтому в нормальном потоке это значение всегда {@code true}.
 */
public record ClockOutResponse(
    UUID timeEntryId,         // ID созданной смены
    UUID employeeId,          // за кого
    LocalDateTime checkOut,    // когда вышел
    boolean isFirstShiftTodayIsOver // предупреждение: "Вы уже закончили смену сегодня" 
) {}