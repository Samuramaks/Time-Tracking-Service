package ru.samura.time_tracking_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Запись учёта рабочего времени — фиксация начала и окончания смены сотрудника.
 * <p>
 * Каждая запись соответствует одной смене (рабочему дню/сессии). Открытие смены (check-in)
 * всегда фиксируется; закрытие смены (check-out) может отсутствовать — тогда смена считается активной (не завершённой).
 * 
 * <p>
 * Флаг {@link #isManual} указывает, была ли смена закрыта вручную (например, администратором при ошибке системы),
 * что может влиять на логику расчёта или аудит.
 * 
 * <p>
 * ⚠️ Временные метки хранятся в {@link LocalDateTime} (без временной зоны).  
 * Предполагается, что все операции выполняются в единой временной зоне (например, UTC или локальной зоне сервера).
 * Для распределённых систем рекомендуется использовать {@link java.time.ZonedDateTime} или {@link java.time.Instant}.
 * 
 *
 * @see Employee
 */
@Entity
@Table(name = "time_entry")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class TimeEntry {

    /**
     * Уникальный идентификатор записи времени.
     * <p>
     * Генерируется автоматически при сохранении в БД (стратегия {@link GenerationType#UUID}).
     * Первичный ключ таблицы {@code time_entry}.
     * 
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Сотрудник, к которому относится данная смена.
     * <p>
     * Обязательное поле. Связь "многие к одному" — один сотрудник может иметь множество записей времени.
     * Внешний ключ {@code employee_id} ссылается на таблицу {@code employees}.
     * 
     *
     * @see Employee
     */
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Время начала смены (регистрация прихода на работу / открытие смены).
     * <p>
     * Обязательное поле. Фиксируется в момент начала работы сотрудника.
     * Не может быть {@code null}.
     * 
     */
    @Column(name = "register", nullable = false)
    private LocalDateTime checkIn;

    /**
     * Время окончания смены (регистрация ухода / закрытие смены).
     * <p>
     * Необязательное поле. Если {@code null} — смена ещё не завершена (активна).
     * При наличии значения — смена считается закрытой.
     * 
     * <p>
     * Логически должно удовлетворять условию: {@code checkOut == null || checkOut.isAfter(checkIn)}.
     * Данное ограничение не выражено на уровне JPA — должна обеспечиваться бизнес-логикой (валидация в сервисе).
     * 
     */
    @Column(name = "check_out")
    private LocalDateTime checkOut;

    /**
     * Флаг ручного завершения смены.
     * <p>
     * {@code true}, если смена была закрыта вручную (например, HR или системным администратором при пропуске автоматического чек-аута).
     * {@code false} (по умолчанию), если смена закрыта автоматически (например, через терминал или мобильное приложение).
     * 
     * <p>
     * Используется для аудита, отчётов и корректировок расчётов (ручные правки могут требовать дополнительного подтверждения).
     * 
     */
    @Column(name = "is_manual")
    private boolean isManual;
}