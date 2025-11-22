package ru.samura.time_tracking_service.response;

import ru.samura.time_tracking_service.DTO.EmployeeDTO;

/**
 * Ответ с расчётом заработной платы сотрудника за отчётный период.
 * <p>
 * Используется в сводных отчётах (например, для HR или бухгалтерии).
 * Все значения выражены в целых единицах (часы, условные денежные единицы).
 * 
 *
 * @param message         пояснительное сообщение (например, "Отчет за месяц: 2025-11")
 * @param employeeDTO     данные сотрудника (ФИО, email и т.д.)
 * @param totalHours      фактически отработанное время за период (в часах)
 * @param expectedHours   ожидаемое (нормативное) рабочее время (в часах)
 *                        ⚠️ Примечание: имя поля содержит опечатку — вероятно, имелось в виду "expectedHours"
 * @param overtime        переработка (разница totalHours - expectedHours, но не ниже 0)
 * @param pay             итоговая сумма к выплате (в условных единицах)
 */
public record TotalPaymentEmployeeResponse(
    String message,
    EmployeeDTO employeeDTO,
    Long totalHours,
    Long expectedHours,
    Long overtime,
    Long pay
) {}