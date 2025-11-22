package ru.samura.time_tracking_service.DTO;

import java.util.UUID;

import lombok.Data;
import ru.samura.time_tracking_service.entity.Employee;
import ru.samura.time_tracking_service.entity.Role;

/**
 * Data Transfer Object (DTO) для передачи информации о сотруднике между слоями приложения
 * (например, из сервиса в контроллер или в ответе API).
 * <p>
 * Содержит только данные, необходимые для отображения или расчётов — без JPA-аннотаций, связей и поведения.
 * Используется для изоляции внутренней модели от внешнего интерфейса (защита от overposting, упрощение сериализации).
 * 
 * <p>
 * ⚠️ Данный класс <strong>не является иммутабельным</strong> (используется {@code @Data}).
 * Для production-кода рекомендуется делать DTO иммутабельными (см. примечания ниже).
 * 
 *
 * @see Employee
 * @see Role
 */
@Data
public class EmployeeDTO {

    /**
     * Уникальный идентификатор сотрудника.
     * <p>
     * Может быть {@code null} при создании нового сотрудника (если ID генерируется на стороне сервера).
     * 
     */
    private UUID id;

    /**
     * Полное имя сотрудника (Фамилия Имя Отчество).
     * <p>
     * Обязательное поле. Не должно быть пустым или {@code null} в валидных объектах.
     * 
     */
    private String fullName;

    /**
     * Адрес электронной почты сотрудника.
     * <p>
     * Обязательное и уникальное поле. Используется для идентификации и уведомлений.
     * Должен соответствовать формату email (валидация рекомендуется на уровне контроллера/сервиса).
     * 
     */
    private String email;

    /**
     * Почасовая ставка оплаты труда (в условных единицах).
     * <p>
     * Обязательное целочисленное значение. Должно быть > 0.
     * 
     */
    private int hourlyRate;

    /**
     * Норма рабочего времени в день (в часах).
     * <p>
     * Обязательное целочисленное значение. Должно быть ≥ 0 (0 допустимо для удалённых/гибких схем).
     * 
     */
    private int workHoursPerDay;

    /**
     * Роль сотрудника в системе.
     * <p>
     * Обязательное поле. Определяет уровень доступа и функциональность.
     * 
     *
     * @see Role
     */
    private Role role;

    /**
     * Создаёт DTO на основе сущности {@link Employee}.
     * <p>
     * Копирует все поля «как есть». Не выполняет глубокую копию (достаточно для простых типов и enum).
     * 
     * <p>
     * ⚠️ Внимание: если {@code employee == null}, выбрасывается {@link NullPointerException}.
     * Для production-кода рекомендуется добавить проверку или использовать {@code Optional}.
     * 
     *
     * @param employee сущность сотрудника, не может быть {@code null}
     * @return новый экземпляр {@link EmployeeDTO}
     * @throws NullPointerException если {@code employee} равен {@code null}
     */
    public static EmployeeDTO fromEntity(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee entity must not be null");
        }
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());
        dto.setHourlyRate(employee.getHourlyRate());
        dto.setWorkHoursPerDay(employee.getWorkHoursPerDay());
        dto.setRole(employee.getRole());
        return dto;
    }

    /**
     * Преобразует DTO обратно в сущность {@link Employee}.
     * <p>
     * Полезно при создании/обновлении сотрудника из входящих данных (например, REST-запроса).
     * 
     * <p>
     * ⚠️ Внимание: конструктор по умолчанию {@link Employee} не инициализирует коллекции или связи.
     * Убедитесь, что все обязательные поля DTO заполнены перед вызовом этого метода.
     * 
     * <p>
     * Этот метод не устанавливает связи «от сотрудника к другим сущностям» (например, {@code timeEntries}),
     * так как DTO их не содержит. Загрузка связанных данных должна происходить отдельно.
     * 
     *
     * @return новая сущность {@link Employee}, не сохранённая в БД
     */
    public Employee toEntity() {
        Employee employee = new Employee();
        employee.setId(this.id);
        employee.setFullName(this.fullName);
        employee.setEmail(this.email);
        employee.setHourlyRate(this.hourlyRate);
        employee.setWorkHoursPerDay(this.workHoursPerDay);
        employee.setRole(this.role);
        return employee;
    }
}