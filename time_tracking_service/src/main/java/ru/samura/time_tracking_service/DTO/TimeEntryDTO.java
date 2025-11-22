package ru.samura.time_tracking_service.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;
import ru.samura.time_tracking_service.entity.TimeEntry;

/**
 * Data Transfer Object (DTO) для передачи информации о записи рабочего времени (смене).
 * <p>
 * Представляет одну сессию работы сотрудника: момент входа, выхода и способ завершения.
 * Используется для API-ответов, отчётов и UI-отображения.
 * 
 * <p>
 * ⚠️ Временные метки ({@link #checkIn}, {@link #checkOut}) представлены как {@link LocalDateTime}
 * без информации о временной зоне. Подразумевается единая временная зона для всей системы
 * (например, UTC или локальная зона сервера). Для клиент-серверных систем рекомендуется
 * использовать {@link java.time.Instant} или {@link java.time.ZonedDateTime} в DTO.
 * 
 *
 * @see TimeEntry
 * @see EmployeeDTO
 */
@Data
public class TimeEntryDTO {

    /**
     * Уникальный идентификатор записи времени.
     * <p>
     * Может быть {@code null} при создании новой записи (если ID генерируется сервером/БД).
     * 
     */
    private UUID id;

    /**
     * Сотрудник, к которому относится данная смена.
     * <p>
     * Обязательное поле в валидных объектах, но может быть {@code null} при частичной загрузке
     * (например, в списках с пагинацией без вложенных данных). Клиент должен быть готов к этому.
     * 
     */
    private EmployeeDTO employee;

    /**
     * Время начала смены (регистрация прихода).
     * <p>
     * Обязательное поле. Не может быть {@code null} в завершённой записи.
     * 
     */
    private LocalDateTime checkIn;

    /**
     * Время окончания смены (регистрация ухода).
     * <p>
     * Необязательное поле. Если {@code null} — смена активна (ещё не завершена).
     * 
     */
    private LocalDateTime checkOut;

    /**
     * Флаг ручного завершения смены.
     * <p>
     * {@code true}, если смена закрыта вручную (например, HR или администратором).
     * {@code false} — автоматически (терминал, приложение).
     * Используется для аудита и корректировок.
     * 
     */
    private boolean isManual;

    /**
     * Создаёт DTO на основе сущности {@link TimeEntry}.
     * <p>
     * Выполняет полную копию данных, включая вложенный {@link EmployeeDTO}.
     * 
     * <p>
     * ⚠️ Требования:
     * <ul>
     *   <li>{@code timeEntry} не должен быть {@code null} — иначе {@link NullPointerException}.</li>
     *   <li>{@code timeEntry.getEmployee()} не должен быть {@code null} — иначе {@link NullPointerException}
     *       при вызове {@link EmployeeDTO#fromEntity}.</li>
     * </ul>
     * Для production-кода рекомендуется добавить проверки или использовать {@code Optional}.
     * 
     *
     * @param timeEntry сущность записи времени, не может быть {@code null}
     * @return новый экземпляр {@link TimeEntryDTO}
     * @throws NullPointerException если {@code timeEntry} или {@code timeEntry.getEmployee()} равны {@code null}
     */
    public static TimeEntryDTO fromEntity(TimeEntry timeEntry) {
        if (timeEntry == null) {
            throw new IllegalArgumentException("TimeEntry entity must not be null");
        }
        if (timeEntry.getEmployee() == null) {
            throw new IllegalArgumentException("Employee in TimeEntry must not be null");
        }

        TimeEntryDTO dto = new TimeEntryDTO();
        dto.setId(timeEntry.getId());
        dto.setEmployee(EmployeeDTO.fromEntity(timeEntry.getEmployee()));
        dto.setCheckIn(timeEntry.getCheckIn());
        dto.setCheckOut(timeEntry.getCheckOut());
        dto.setManual(timeEntry.isManual());
        return dto;
    }

    /**
     * Преобразует DTO обратно в сущность {@link TimeEntry}.
     * <p>
     * Полезно для создания/обновления записи на основе входящих данных (например, REST-запроса).
     * 
     * <p>
     * Особенности:
     * <ul>
     *   <li>Если {@link #employee} равен {@code null}, поле {@code employee} в сущности останется {@code null}
     *       — это может вызвать нарушение ограничения {@code NOT NULL} при сохранении в БД.</li>
     *   <li>Объект {@link TimeEntry} создаётся через конструктор по умолчанию — связи (например, коллекции)
     *       не инициализируются.</li>
     * </ul>
     * 
     * <p>
     * ⚠️ Рекомендуется выполнять валидацию перед вызовом (например, в сервисе):
     * <pre>
     * if (dto.getEmployee() == null || dto.getCheckIn() == null) {
     *     throw new IllegalArgumentException("Employee and checkIn are required");
     * }
     * </pre>
     * 
     *
     * @return новая сущность {@link TimeEntry}, не сохранённая в БД
     */
    public TimeEntry toEntity() {
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setId(this.id);
        if (this.employee != null) {
            timeEntry.setEmployee(this.employee.toEntity());
        }
        timeEntry.setCheckIn(this.checkIn);
        timeEntry.setCheckOut(this.checkOut);
        timeEntry.setManual(this.isManual);
        return timeEntry;
    }
}