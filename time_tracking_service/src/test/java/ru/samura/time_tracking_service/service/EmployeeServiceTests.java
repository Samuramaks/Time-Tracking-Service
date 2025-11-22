package ru.samura.time_tracking_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ru.samura.time_tracking_service.DTO.EmployeeDTO;
import ru.samura.time_tracking_service.entity.Role;
import ru.samura.time_tracking_service.repository.EmployeeRepository;

public class EmployeeServiceTests {
    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHasAccess_HR(){
        EmployeeDTO currentUser = new EmployeeDTO();
        currentUser.setRole(Role.HR);
        UUID targetEmployeeId = UUID.randomUUID();

        boolean result = employeeService.hasAccess(currentUser, targetEmployeeId);

        assertTrue(result, "HR имеет доступ ко всем сотрудникам");
    }

    @Test
    void testHasAccess_Employee_WithOwnData(){
        EmployeeDTO currentUser = new EmployeeDTO();
        UUID targetEmployeeId = UUID.randomUUID();
        currentUser.setRole(Role.EMPLOYEE);
        currentUser.setId(targetEmployeeId);
        

        boolean result = employeeService.hasAccess(currentUser, targetEmployeeId);

        assertTrue(result, "Сотрудник имеет доступ к своим данным");
    }

    @Test
    void testHasAccess_Employee_WithOtherData(){
        EmployeeDTO currentUser = new EmployeeDTO();
        currentUser.setRole(Role.EMPLOYEE);
        UUID targetEmployeeId = UUID.randomUUID();

        boolean result = employeeService.hasAccess(currentUser, targetEmployeeId);

        assertFalse(result, "Сотрудник не имеет доступа к чужим данным");
    }

    @Test
    void testHasAcсess_NullArguments(){
        EmployeeDTO currentUser = null;
        UUID targetEmployeeId = UUID.randomUUID();


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.hasAccess(currentUser, targetEmployeeId);
        });

        assertEquals("Аргументы не должны быть null", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.hasAccess(new EmployeeDTO(), null);
        });

        assertEquals("Аргументы не должны быть null", exception.getMessage());
    }
}
