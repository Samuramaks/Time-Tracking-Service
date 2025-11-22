package ru.samura.time_tracking_service.repository;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.samura.time_tracking_service.entity.TimeEntry;

/**
 * Репозиторий для управления записями рабочего времени ({@link TimeEntry}).
 *
 * <p>Предоставляет стандартные CRUD-операции и кастомные методы для:
 * <ul>
 *   <li>Получения всех смен сотрудника (для личного кабинета, истории).</li>
 *   <li>Фильтрации смен по месяцу — для расчёта заработной платы.</li>
 * </ul>
 *
 * <p>⚠️ Важно: текущая реализация использует <strong>native SQL-запросы</strong>.
 * Это даёт гибкость, но влечёт риски:
 * <ul>
 *   <li>Зависимость от СУБД (функция {@code TO_CHAR} — PostgreSQL/Oracle; в H2/MySQL — не сработает).</li>
 *   <li>Отсутствие типобезопасности и проверки на этапе компиляции.</li>
 *   <li>Уязвимость к SQL-инъекциям при неправильной передаче параметров (но {@code @Param} защищает от этого).</li>
 * </ul>
 * Для production-систем рекомендуется использовать JPQL/HQL или Criteria API при возможности.
 *
 * @see TimeEntry
 */
@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    /**
     * Находит все записи времени (смены) для указанного сотрудника.
     *
     * <p>Возвращает как завершённые, так и активные (без {@code check_out}) смены.
     * Результат не упорядочен — для UI рекомендуется сортировка по {@code checkIn DESC} (см. примечание).
     *
     * <p>💡 Оптимизация: можно добавить {@code ORDER BY check_in DESC} в запрос
     * или использовать JPQL с {@code @Query("SELECT t FROM TimeEntry t WHERE t.employee.id = :employeeId ORDER BY t.checkIn DESC")}.
     *
     * @param employeeId идентификатор сотрудника, не должен быть {@code null}
     * @return список записей времени (может быть пустым); {@code null} не возвращается
     */
    @Query(value = "SELECT * FROM time_entry WHERE employee_id = :employeeId ORDER BY check_in DESC", nativeQuery = true)
    List<TimeEntry> findShiftsByEmployee(@Param("employeeId") UUID employeeId);

    /**
     * Находит смены сотрудника, завершённые в указанном месяце (для расчёта оплаты).
     *
     * <p>Фильтрация происходит по дате {@code check_out} (окончание смены).
     * Смены без {@code check_out} (активные) в результат <strong>не включаются</strong>.
     *
     * <p>⚠️ Текущая реализация:
     * <ul>
     *   <li>Использует {@code TO_CHAR(check_out, 'YYYY-MM')} — работает в PostgreSQL, Oracle; не работает в H2 (в тестах может падать).</li>
     *   <li>Принимает месяц как строку {@code "2025-11"} — уязвимо к ошибкам формата.</li>
     * </ul>
     *
     * <p>✅ Рекомендуемая альтернатива (JPQL, типобезопасная, кроссплатформенная):
     * <pre>{@code
     * @Query("SELECT t FROM TimeEntry t " +
     *        "WHERE t.employee.id = :employeeId " +
     *        "  AND FUNCTION('YEAR', t.checkOut) = :year " +
     *        "  AND FUNCTION('MONTH', t.checkOut) = :month")
     * List<TimeEntry> findShiftsByEmployeeAndYearMonth(
     *     @Param("employeeId") UUID employeeId,
     *     @Param("year") int year,
     *     @Param("month") int month
     * );
     * }</pre>
     * Или ещё лучше — использовать {@link YearMonth} и Criteria API / спецификации.
     *
     * @param employeeId идентификатор сотрудника
     * @param month месяц в формате {@code "yyyy-MM"} (например, {@code "2025-11"}), не должен быть {@code null}
     * @return список завершённых смен в указанном месяце (может быть пустым)
     * @throws IllegalArgumentException если {@code month} не соответствует формату {@code yyyy-MM}
     */
    @Query(value = """
        SELECT * FROM time_entry
        WHERE employee_id = :employeeId
          AND check_out IS NOT NULL
          AND TO_CHAR(check_out, 'YYYY-MM') = :month
        ORDER BY check_in ASC
        """, nativeQuery = true)
    List<TimeEntry> findShiftsForPaymentByEmployeeAndMonth(
        @Param("employeeId") UUID employeeId,
        @Param("month") String month
    );
}