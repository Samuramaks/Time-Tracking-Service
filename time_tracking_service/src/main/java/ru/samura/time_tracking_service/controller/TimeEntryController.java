package ru.samura.time_tracking_service.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.samura.time_tracking_service.DTO.EmployeeDTO;
import ru.samura.time_tracking_service.DTO.TimeEntryDTO;
import ru.samura.time_tracking_service.custom_exception.ActiveShiftTodayException;
import ru.samura.time_tracking_service.custom_exception.NoShiftFoundException;
import ru.samura.time_tracking_service.response.ClockInResponse;
import ru.samura.time_tracking_service.response.ClockOutResponse;
import ru.samura.time_tracking_service.service.EmployeeService;
import ru.samura.time_tracking_service.service.TimeEntryService;

/**
 * Контроллер для управления сменами: открытие (clock-in) и закрытие (clock-out) рабочего дня.
 * <p>
 * Поддерживает как самостоятельные действия сотрудника, так и вмешательство HR.
 * 
 */
@RestController
@RequestMapping("/time-entry")
public class TimeEntryController {

    @Autowired
    TimeEntryService timeEntryService;

    @Autowired
    EmployeeService employeeService;

    /**
     * Регистрирует начало смены («приход») для указанного сотрудника.
     * <p>
     * Проверяет доступ: в текущей реализации {@link EmployeeService#hasAccess}
     * разрешает операцию, только если сотрудник — HR и указал <strong>свой</strong> ID.
     * Это, вероятно, опечатка в логике (ожидается обратное: запрет, если нет доступа).
     * 
     * <p>
     * При успешной регистрации возвращает {@link ClockInResponse} с данными новой смены.
     * 
     *
     * @param employeeId идентификатор сотрудника, для которого открывается смена
     * @return {@code 200 OK} + ответ с данными смены, <strong>или</strong>
     *         {@code 403 Forbidden} + заглушка {@link ClockInResponse} с {@code null}-полями, если доступ запрещён
     * @throws ru.samura.time_tracking_service.custom_exception.ActiveShiftTodayException если смена уже активна → {@code 500} (без @ControllerAdvice)
     */
    @GetMapping("/employees/{employeeId}/clock-in")
    public ResponseEntity<ClockInResponse> clockIn(@PathVariable("employeeId") UUID employeeId) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeInfo(employeeId);

        if (!employeeService.hasAccess(employeeDTO, employeeId)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ClockInResponse(null, employeeDTO.getId(), null, false));
        }

        TimeEntryDTO timeEntryDTO = timeEntryService.clockIn(employeeDTO);

        ClockInResponse response = new ClockInResponse(timeEntryDTO.getId(), 
                                                       employeeId, 
                                                       timeEntryDTO.getCheckIn(),
                                                        true);

        return ResponseEntity.ok(response);
    }

    /**
     * Регистрирует окончание смены («уход») для сотрудника, инициированное им самим.
     * <p>
     * Аналогично {@link #clockIn}, проверяет доступ через {@code hasAccess}.
     * Закрывает последнюю активную смену сотрудника.
     * 
     *
     * @param employeeId идентификатор сотрудника
     * @return {@code 200 OK} + {@link ClockOutResponse}, <strong>или</strong>
     *         {@code 403 Forbidden} + заглушка при отсутствии доступа
     * @throws ru.samura.time_tracking_service.custom_exception.NoShiftFoundException если смен нет
     * @throws ru.samura.time_tracking_service.custom_exception.ClosedShiftTodayException если смена уже закрыта
     */
    @GetMapping("/employees/{employeeId}/clock-out")
    public ResponseEntity<ClockOutResponse> clockOut(@PathVariable("employeeId") UUID employeeId) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeInfo(employeeId);
        if (!employeeService.hasAccess(employeeDTO, employeeId)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ClockOutResponse(null, employeeDTO.getId(), null, false));
        }

        TimeEntryDTO timeEntryDTO = timeEntryService.clockOut(employeeDTO, false);

        ClockOutResponse response = new ClockOutResponse(timeEntryDTO.getId(), 
                                                        employeeDTO.getId(), 
                                                        timeEntryDTO.getCheckOut(), 
                                                        true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Регистрирует окончание смены для другого сотрудника от имени HR.
     * <p>
     * Принимает два ID: HR и целевого сотрудника.
     * Проверяет доступ HR к данным целевого сотрудника через {@code hasAccess(hrDTO, employeeId)}.
     * При успехе закрывает смену сотрудника с флагом {@code isManual = true}.
     * 
     *
     * @param hrId       идентификатор HR-пользователя
     * @param employeeId идентификатор сотрудника, чью смену закрывает HR
     * @return {@code 200 OK} + {@link ClockOutResponse}, <strong>или</strong>
     *         {@code 403 Forbidden} + заглушка, если HR не имеет доступа к сотруднику
     * @throws ActiveShiftTodayException если у сотрудника уже есть активная смена
     * @throws NoShiftFoundException если смены для закрытия не найдены
     */
    @GetMapping("/employees/{hrId}/{employeeId}/clock-out")
    public ResponseEntity<ClockOutResponse> clockOutByHRForEmployee(@PathVariable("hrId") UUID hrId, @PathVariable("employeeId") UUID employeeId) {
        EmployeeDTO hrDTO = employeeService.getEmployeeInfo(hrId);
        EmployeeDTO employeeDTO = employeeService.getEmployeeInfo(employeeId);
        
        if (!employeeService.hasAccess(hrDTO, employeeId)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ClockOutResponse(null, hrDTO.getId(), null, false));
        }
        
        TimeEntryDTO timeEntryDTO = timeEntryService.clockOut(employeeDTO, true);

        ClockOutResponse response = new ClockOutResponse(timeEntryDTO.getId(), 
                                                        employeeDTO.getId(), 
                                                        timeEntryDTO.getCheckOut(), 
                                                        true);
        
        return ResponseEntity.ok(response);
    }
}