package ru.samura.time_tracking_service.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ru.samura.time_tracking_service.DTO.EmployeeDTO;
import ru.samura.time_tracking_service.custom_exception.UserNotFoundException;
import ru.samura.time_tracking_service.entity.Employee;
import ru.samura.time_tracking_service.entity.Role;
import ru.samura.time_tracking_service.repository.EmployeeRepository;

/**
 * Сервис для получения информации о сотрудниках и проверки прав доступа.
 * <p>
 * Обеспечивает кеширование данных для повышения производительности.
 * 
 */
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Возвращает информацию о сотруднике по его уникальному идентификатору.
     * <p>
     * Результат кешируется в кеше с именем {@code "employeeCache"} с ключом — значением {@code id}.
     * Если сотрудник не найден, выбрасывается {@link UserNotFoundException}.
     * 
     *
     * @param id идентификатор сотрудника
     * @return DTO с данными сотрудника
     * @throws UserNotFoundException если сотрудник с указанным ID не существует
     */
    @Cacheable(value = "employeeCache", key = "#id")
    public EmployeeDTO getEmployeeInfo(UUID id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() ->
            new UserNotFoundException("Пользователь не найден"));

        return EmployeeDTO.fromEntity(employee);
    }

    /**
     * Проверяет, имеет ли текущий пользователь (представленный employeeDTO)
     * право просматривать/редактировать данные сотрудника с указанным ID.
     * <p>
     * Правила доступа:
     * <ul>
     *   <li>HR имеет доступ ко всем сотрудникам (включая себя),</li>
     *   <li>Рядовой сотрудник (EMPLOYEE) имеет доступ только к своим собственным данным.</li>
     * </ul>
     *
     * @param currentUserDTO данные авторизованного пользователя (не null)
     * @param targetEmployeeId ID сотрудника, к которому запрашивается доступ (не null)
     * @return true, если доступ разрешён
     * @throws IllegalArgumentException если аргументы null
     */
    public boolean hasAccess(EmployeeDTO currentUserDTO, UUID targetEmployeeId) {
        if (currentUserDTO == null || targetEmployeeId == null) {
            throw new IllegalArgumentException("Аргументы не должны быть null");
        }

        Role role = currentUserDTO.getRole();
        UUID currentUserId = currentUserDTO.getId();

        // HR — полный доступ ко всем
        if (Role.HR.equals(role)) {
            return true;
        }

        // EMPLOYEE — только к себе
        return currentUserId != null && currentUserId.equals(targetEmployeeId);
    }

    /**
     * Возвращает список всех сотрудников.
     * <p>
     * Результат кешируется в кеше с именем {@code "getInfoAboutAllEmployee"}.
     * Если в системе нет сотрудников, выбрасывается {@link UserNotFoundException}.
     * 
     *
     * @return список DTO всех сотрудников
     * @throws UserNotFoundException если ни одного сотрудника не найдено
     */
    @Cacheable(value = "getInfoAboutAllEmployee")
    public List<EmployeeDTO> getInfoAboutAllEmployee() {
        List<EmployeeDTO> employeeDTO = employeeRepository.findAll().stream()
                                            .map(EmployeeDTO::fromEntity)
                                            .collect(Collectors.toList());
        if (employeeDTO.isEmpty()) {
            throw new UserNotFoundException("Пользователи не найден");
        }

        return employeeDTO;
    }
}