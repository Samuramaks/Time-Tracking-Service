package ru.samura.time_tracking_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



import ru.samura.time_tracking_service.DTO.EmployeeDTO;
import ru.samura.time_tracking_service.DTO.TimeEntryDTO;
import ru.samura.time_tracking_service.custom_exception.ActiveShiftTodayException;
import ru.samura.time_tracking_service.entity.Role;
import ru.samura.time_tracking_service.entity.TimeEntry;
import ru.samura.time_tracking_service.repository.TimeEntryRepository;

public class TimeEntryServiceTests {
    
    @InjectMocks
    private TimeEntryService timeEntryService;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testClockIn_Success() {
        // Создание сотрудника
        EmployeeDTO currentUser = new EmployeeDTO();
        UUID targetEmployeeId = UUID.randomUUID();
        currentUser.setId(targetEmployeeId);
        currentUser.setRole(Role.EMPLOYEE);

        // Создание обьекта TimeEntry
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setEmployee(currentUser.toEntity());
        timeEntry.setCheckIn(LocalDateTime.now()); // Устанавливаем checkIn
        
        // Настройка мока
        when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(timeEntry);
        
        // Вызов метода clockIn
        TimeEntryDTO result = timeEntryService.clockIn(currentUser);

        // Проверка результатов
        assertNotNull(result.getCheckIn(), "Поле checkIn не должно быть null");
        assertFalse(result.isManual(), "isManual должно быть false");

        // Проверка, что метод save был вызван один раз
        verify(timeEntryRepository, times(1)).save(any(TimeEntry.class));
    }

    @Test
    void testClockIn_AccessDenied() {
        // Создание сотрудника
        EmployeeDTO currentUser = new EmployeeDTO();
        UUID targetEmployeeId = UUID.randomUUID();
        currentUser.setId(targetEmployeeId);
        currentUser.setRole(Role.EMPLOYEE);

        // Создание активной смены
        TimeEntry activeShift = new TimeEntry();
        activeShift.setCheckIn(LocalDateTime.now());
        activeShift.setEmployee(currentUser.toEntity());
        activeShift.setCheckOut(null); // Указываем, что смена активна

        // Настройка мока для метода findShiftByEmployee
        when(timeEntryRepository.findShiftsByEmployee(targetEmployeeId)).thenReturn(List.of(activeShift));

        // Ожидание исключения при попытке регистрации второй смены
        ActiveShiftTodayException exception = assertThrows(ActiveShiftTodayException.class, () -> {
            timeEntryService.clockIn(currentUser); // Здесь происходит проверка на активную смену
        });

        // Проверка сообщения исключения
        assertEquals("У пользователя уже есть активная смена сегодня", exception.getMessage());
    }


}
