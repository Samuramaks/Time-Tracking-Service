package ru.samura.time_tracking_service.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import ru.samura.time_tracking_service.DTO.EmployeeDTO;
import ru.samura.time_tracking_service.service.EmployeeService;

/**
 * Контроллер для получения информации о сотрудниках.
 * <p>
 * Предоставляет endpoint’ы для просмотра данных одного сотрудника и списка всех сотрудников
 * с контролем доступа на основе роли.
 * 
 */
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    /**
     * Возвращает информацию о сотруднике по его уникальному идентификатору.
     * <p>
     * Не требует проверки доступа — предполагается, что запрос инициируется самим сотрудником
     * или системой с доверенным контекстом.
     * 
     *
     * @param employeeId идентификатор сотрудника
     * @return {@code 200 OK} + данные сотрудника в формате JSON
     * @throws ru.samura.time_tracking_service.custom_exception.UserNotFoundException если сотрудник не найден → {@code 404}
     */
    @GetMapping("/{employee_id}/info")
    public ResponseEntity<EmployeeDTO> getEmployeeInfo(@PathVariable("employee_id") UUID employeeId) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeInfo(employeeId);

        return ResponseEntity.ok(employeeDTO);
    }

    /**
     * Возвращает список всех сотрудников.
     * <p>
     * Доступ разрешён только при наличии прав (проверка через {@link EmployeeService#hasAccess}).
     * В текущей реализации {@code hasAccess} требует, чтобы:
     * <ul>
     *   <li>запрашиваемый {@code employeeId} совпадал с ID текущего пользователя,</li>
     *   <li>и у пользователя была роль {@link ru.samura.time_tracking_service.entity.Role#HR}.</li>
     * </ul>
     * То есть — только HR может запросить список, и только указав <strong>свой собственный</strong> ID в пути.
     * 
     *
     * @param employeeId ID сотрудника, запрашивающего список (обычно — его собственный)
     * @return {@code 200 OK} + список всех сотрудников, <strong>или</strong>
     *         {@code 403 Forbidden} + пустой список, если доступ запрещён
     */
    @GetMapping("/{employee_id}/all-info")
    public ResponseEntity<List<EmployeeDTO>> getInfoAboutAllEmployee(@PathVariable("employee_id") UUID employeeId) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeInfo(employeeId);

        if (!employeeService.hasAccess(employeeDTO, employeeId)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ArrayList<>());
        }

        List<EmployeeDTO> employeeDTOs = employeeService.getInfoAboutAllEmployee();
        
        return ResponseEntity.ok(employeeDTOs);
    }
}