package ru.samura.time_tracking_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ru.samura.time_tracking_service.DTO.EmployeeDTO;
import ru.samura.time_tracking_service.DTO.TimeEntryDTO;
import ru.samura.time_tracking_service.custom_exception.ActiveShiftTodayException;
import ru.samura.time_tracking_service.custom_exception.ClosedShiftTodayException;
import ru.samura.time_tracking_service.custom_exception.NoShiftFoundException;
import ru.samura.time_tracking_service.entity.Role;
import ru.samura.time_tracking_service.response.ClockInResponse;
import ru.samura.time_tracking_service.response.ClockOutResponse;
import ru.samura.time_tracking_service.service.EmployeeService;
import ru.samura.time_tracking_service.service.TimeEntryService;

class TimeEntryControllerTests {

    @InjectMocks
    private TimeEntryController timeEntryController;

    @Mock
    private TimeEntryService timeEntryService;

    @Mock
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testClockIn_Success() {
        UUID employeeId = UUID.randomUUID();
        
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employeeId);
        employeeDTO.setRole(Role.EMPLOYEE);

        TimeEntryDTO timeEntryDTO = new TimeEntryDTO();
        timeEntryDTO.setId(UUID.randomUUID());
        timeEntryDTO.setCheckIn(LocalDateTime.now());

        when(employeeService.getEmployeeInfo(employeeId)).thenReturn(employeeDTO);
        when(employeeService.hasAccess(employeeDTO, employeeId)).thenReturn(true);
        when(timeEntryService.clockIn(employeeDTO)).thenReturn(timeEntryDTO);

        ResponseEntity<ClockInResponse> response = timeEntryController.clockIn(employeeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(timeEntryDTO.getId(), response.getBody().timeEntryId());
    }

    @Test
    void testClockIn_AccessDenied() {
        UUID employeeId = UUID.randomUUID();
        
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employeeId);
        employeeDTO.setRole(Role.EMPLOYEE);

        when(employeeService.getEmployeeInfo(employeeId)).thenReturn(employeeDTO);
        when(employeeService.hasAccess(employeeDTO, employeeId)).thenReturn(false);

        ResponseEntity<ClockInResponse> response = timeEntryController.clockIn(employeeId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().timeEntryId());
    }

    @Test
    void testClockIn_ActiveShift() {
        UUID employeeId = UUID.randomUUID();
        
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employeeId);
        employeeDTO.setRole(Role.EMPLOYEE);

        when(employeeService.getEmployeeInfo(employeeId)).thenReturn(employeeDTO);
        when(employeeService.hasAccess(employeeDTO, employeeId)).thenReturn(true);
        doThrow(new ActiveShiftTodayException("У пользователя уже есть активная смена сегодня"))
            .when(timeEntryService).clockIn(employeeDTO);

        ActiveShiftTodayException exception = assertThrows(ActiveShiftTodayException.class, () -> {
            timeEntryController.clockIn(employeeId);
        });

        assertEquals("У пользователя уже есть активная смена сегодня", exception.getMessage());
    }

    @Test
    void testClockOut_Success() {
        UUID employeeId = UUID.randomUUID();
        
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employeeId);
        employeeDTO.setRole(Role.EMPLOYEE);

        TimeEntryDTO timeEntryDTO = new TimeEntryDTO();
        timeEntryDTO.setId(UUID.randomUUID());
        timeEntryDTO.setCheckOut(LocalDateTime.now());

        when(employeeService.getEmployeeInfo(employeeId)).thenReturn(employeeDTO);
        when(employeeService.hasAccess(employeeDTO, employeeId)).thenReturn(true);
        when(timeEntryService.clockOut(employeeDTO, false)).thenReturn(timeEntryDTO);

        ResponseEntity<ClockOutResponse> response = timeEntryController.clockOut(employeeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(timeEntryDTO.getId(), response.getBody().timeEntryId());
    }

    @Test
    void testClockOut_AccessDenied() {
        UUID employeeId = UUID.randomUUID();
        
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employeeId);
                employeeDTO.setRole(Role.EMPLOYEE);

        when(employeeService.getEmployeeInfo(employeeId)).thenReturn(employeeDTO);
        when(employeeService.hasAccess(employeeDTO, employeeId)).thenReturn(false);

        ResponseEntity<ClockOutResponse> response = timeEntryController.clockOut(employeeId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().timeEntryId());
    }

    @Test
    void testClockOut_NoShiftFound() {
        UUID employeeId = UUID.randomUUID();
        
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employeeId);
        employeeDTO.setRole(Role.EMPLOYEE);

        when(employeeService.getEmployeeInfo(employeeId)).thenReturn(employeeDTO);
        when(employeeService.hasAccess(employeeDTO, employeeId)).thenReturn(true);
        when(timeEntryService.clockOut(employeeDTO, false)).thenThrow(new NoShiftFoundException("Смены для закрытия не найдены"));

        NoShiftFoundException exception = assertThrows(NoShiftFoundException.class, () -> {
            timeEntryController.clockOut(employeeId);
        });

        assertEquals("Смены для закрытия не найдены", exception.getMessage());
    }

    @Test
    void testClockOut_ClosedShiftToday() {
        UUID employeeId = UUID.randomUUID();
        
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employeeId);
        employeeDTO.setRole(Role.EMPLOYEE);

        when(employeeService.getEmployeeInfo(employeeId)).thenReturn(employeeDTO);
        when(employeeService.hasAccess(employeeDTO, employeeId)).thenReturn(true);
        when(timeEntryService.clockOut(employeeDTO, false)).thenThrow(new ClosedShiftTodayException("Смена уже закрыта"));

        ClosedShiftTodayException exception = assertThrows(ClosedShiftTodayException.class, () -> {
            timeEntryController.clockOut(employeeId);
        });

        assertEquals("Смена уже закрыта", exception.getMessage());
    }
}

       
