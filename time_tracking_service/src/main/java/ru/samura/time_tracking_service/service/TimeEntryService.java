package ru.samura.time_tracking_service.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ru.samura.time_tracking_service.DTO.CalculationPayment;
import ru.samura.time_tracking_service.DTO.EmployeeDTO;
import ru.samura.time_tracking_service.DTO.TimeEntryDTO;
import ru.samura.time_tracking_service.custom_exception.ActiveShiftTodayException;
import ru.samura.time_tracking_service.custom_exception.ClosedShiftTodayException;
import ru.samura.time_tracking_service.custom_exception.NoShiftFoundException;
import ru.samura.time_tracking_service.entity.Role;
import ru.samura.time_tracking_service.entity.TimeEntry;
import ru.samura.time_tracking_service.repository.TimeEntryRepository;
import ru.samura.time_tracking_service.response.TotalPaymentEmployeeResponse;

/**
 * Сервис для управления сменами сотрудников: открытие/закрытие смен, проверка активных смен,
 * расчёт заработной платы за период.
 * <p>
 * Включает кеширование, валидацию состояния смены и агрегацию данных для отчётов.
 * 
 */
@Service
public class TimeEntryService {

    /**
     * Константа: количество рабочих дней в месяце, используемая при расчёте норматива.
     * <p>
     * Используется в {@link #totalPaymentEmployee(EmployeeDTO, String)}.
     * 
     */
    final int WORKDAYS = 20;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    /**
     * Возвращает список всех смен сотрудника (активных и завершённых).
     * <p>
     * Результат кешируется в {@code shiftCache} с ключом {@code employeeId}.
     * 
     *
     * @param employeeId идентификатор сотрудника
     * @return список записей времени; может быть пустым
     */
    @Cacheable(value = "shiftCache", key = "#id")
    public List<TimeEntry> findShiftByEmployee(UUID employeeId) {
        return timeEntryRepository.findShiftsByEmployee(employeeId);
    }

    /**
     * Проверяет, есть ли у сотрудника активная (не завершённая) смена сегодня.
     * <p>
     * Активной считается смена с {@code checkOut == null} и {@code checkIn} в текущей дате.
     * 
     *
     * @param employeeId идентификатор сотрудника
     * @return {@code true}, если активная смена сегодня существует; {@code false} — иначе
     */
    public boolean doesEmployeeHaveActiveShiftToday(UUID employeeId) {
        List<TimeEntry> shifts = findShiftByEmployee(employeeId);

        return shifts.stream().anyMatch(shift -> 
            shift.getCheckOut() == null &&
            shift.getCheckIn().toLocalDate().equals(LocalDate.now())
        );
    }

    /**
     * Проверяет, завершена ли переданная смена сегодня (обе метки — в текущей дате).
     * <p>
     * Учитывает только смены, у которых {@code checkOut != null}.
     * 
     *
     * @param shift запись времени
     * @return {@code true}, если смена закрыта и обе временные метки относятся к сегодняшнему дню
     */
    public boolean doesEmployeeHaveClosedShiftToday(TimeEntry shift) {
        return shift.getCheckOut() != null && 
               shift.getCheckIn().toLocalDate().equals(LocalDate.now()) &&
               shift.getCheckOut().toLocalDate().equals(LocalDate.now());
    }

    /**
     * Регистрирует начало смены («приход») для сотрудника.
     * <p>
     * Перед открытием проверяется отсутствие активной смены сегодня.
     * Для HR устанавливается флаг {@code isManual = true}.
     * 
     *
     * @param employeeDTO данные сотрудника
     * @return DTO созданной смены
     * @throws ActiveShiftTodayException если у сотрудника уже есть активная смена сегодня
     */
    public TimeEntryDTO clockIn(EmployeeDTO employeeDTO) {
        if (doesEmployeeHaveActiveShiftToday(employeeDTO.getId())) {
            throw new ActiveShiftTodayException("У пользователя уже есть активная смена сегодня");
        }
        TimeEntry timeEntry = new TimeEntry();

        timeEntry.setEmployee(employeeDTO.toEntity());
        timeEntry.setCheckIn(LocalDateTime.now());

        if (employeeDTO.getRole().equals(Role.HR)) {
            timeEntry.setManual(true);
        } else {
            timeEntry.setManual(false);
        }

        TimeEntry savedTimeEntry = timeEntryRepository.save(timeEntry);

        return TimeEntryDTO.fromEntity(savedTimeEntry);
    }

    /**
     * Регистрирует окончание смены («уход») для сотрудника.
     * <p>
     * Берёт последнюю смену из списка (предполагается, что она активна).
     * Если смена уже закрыта сегодня — выбрасывается исключение.
     * При вызове от HR устанавливается {@code isManual = true}.
     * 
     *
     * @param employeeDTO данные сотрудника
     * @param isHR {@code true}, если операцию выполняет HR-пользователь
     * @return DTO обновлённой (закрытой) смены
     * @throws NoShiftFoundException если у сотрудника нет ни одной смены
     * @throws ClosedShiftTodayException если последняя смена уже закрыта сегодня
     */
    public TimeEntryDTO clockOut(EmployeeDTO employeeDTO, boolean isHR) {
        TimeEntry shift = findShiftByEmployee(employeeDTO.getId()).stream().reduce((first, second) -> second)
                    .orElseThrow(() -> new NoShiftFoundException("Нет смен для закрытия"));

        if (doesEmployeeHaveClosedShiftToday(shift)) {
            throw new ClosedShiftTodayException("Нельзя перезакрыть закрытую смену");
        }

        shift.setCheckOut(LocalDateTime.now());

        if (isHR) {
            shift.setManual(true);
        }

        TimeEntry savedTimeEntry = timeEntryRepository.save(shift);

        return TimeEntryDTO.fromEntity(savedTimeEntry);
    }

    /**
     * Рассчитывает заработную плату сотрудника за указанный месяц.
     * <p>
     * Агрегирует все завершённые смены за месяц, суммирует часы,
     * сравнивает с нормативом ({@code WORKDAYS × workHoursPerDay}),
     * рассчитывает переработку (но не учитывает недоработку — {@code overtime} ≥ 0),
     * и итоговую оплату (без коэффициентов за переработку).
     * 
     *
     * @param employeeDTO данные сотрудника
     * @param month месяц в формате {@code "yyyy-MM"} (например, {@code "2025-11"})
     * @return DTO с результатами расчёта
     */
    public CalculationPayment totalPaymentEmployee(EmployeeDTO employeeDTO, String month) {
        long totalHours = 0;
        List<TimeEntry> timeEntries = timeEntryRepository.findShiftsForPaymentByEmployeeAndMonth(employeeDTO.getId(), month);
        for (TimeEntry time : timeEntries) {
            LocalDateTime checkIn = time.getCheckIn();
            LocalDateTime checkOut = time.getCheckOut();

            Duration duration = Duration.between(checkIn, checkOut);

            totalHours += duration.toHours();
        }

        long expectedHours = WORKDAYS * employeeDTO.getWorkHoursPerDay();
        long overtime = totalHours - expectedHours;
        long payment = totalHours * employeeDTO.getHourlyRate();
        return new CalculationPayment(employeeDTO, totalHours, expectedHours, overtime, payment);
    }

    /**
     * Рассчитывает заработную плату для списка сотрудников за указанный месяц.
     *
     * @param employeeDTO список сотрудников
     * @param month месяц в формате {@code "yyyy-MM"}
     * @return список расчётов по каждому сотруднику
     */
    public List<CalculationPayment> totalPaymentEmployeeAll(List<EmployeeDTO> employeeDTO, String month) {
        List<CalculationPayment> calculationPayments = new ArrayList<>();

        for (EmployeeDTO dto : employeeDTO) {
            calculationPayments.add(totalPaymentEmployee(dto, month));
        }

        return calculationPayments;
    }

    /**
     * Формирует список ответов для отчёта по заработной плате всех сотрудников за месяц.
     * <p>
     * Оборачивает результаты расчётов в {@link TotalPaymentEmployeeResponse}
     * с общим сообщением-заголовком.
     * 
     *
     * @param employeeDTOs список сотрудников
     * @param month месяц в формате {@code "yyyy-MM"}
     * @return список ответов для API-отчёта
     */
    public List<TotalPaymentEmployeeResponse> getTotalPaymentsForEmployees(List<EmployeeDTO> employeeDTOs, String month) {
        List<TotalPaymentEmployeeResponse> totalPaymentEmployeeResponses = new ArrayList<>();

        List<CalculationPayment> calculationPayments = totalPaymentEmployeeAll(employeeDTOs, month);
        String message = "Отчет за месяц: " + month;

        for (CalculationPayment calculationPayment : calculationPayments) {
            totalPaymentEmployeeResponses.add(new TotalPaymentEmployeeResponse(
                message,
                calculationPayment.getEmployeeDTO(),
                calculationPayment.getTotalHours(),
                calculationPayment.getExpectedHours(),
                calculationPayment.getOvertime(),
                calculationPayment.getPay()));
        }

        return totalPaymentEmployeeResponses;
    }
}