package ru.samura.time_tracking_service.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Сущность сотрудника в системе учёта рабочего времени.
 * <p>
 * Представляет работника организации с базовой информацией, необходимой для расчёта оплаты труда
 * и учёта отработанных часов. Все поля, кроме идентификатора, обязательны для заполнения.
 * 
 *
 * @see Role
 */
@Entity
@Table(name = "employees")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Employee {

    /**
     * Уникальный идентификатор сотрудника.
     * <p>
     * Генерируется автоматически при сохранении сущности в БД (стратегия {@link GenerationType#UUID}).
     * Соответствует первичному ключу в таблице {@code employees}.
     * 
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Полное имя сотрудника (Фамилия Имя Отчество).
     * <p>
     * Обязательное поле. Хранится в столбце {@code FIO} (латиницей, но семантически — кириллица).
     * Не может быть {@code null}.
     * 
     */
    @Column(name = "FIO", nullable = false)
    private String fullName;

    /**
     * Адрес электронной почты сотрудника.
     * <p>
     * Обязательное и уникальное поле. Используется как логин или идентификатор для уведомлений и аутентификации.
     * Не может быть {@code null}; дубликаты запрещены на уровне БД.
     * 
     */
    @Column(unique = true, name = "email", nullable = false)
    private String email;

    /**
     * Почасовая ставка оплаты труда (в условных единицах, например, рублях).
     * <p>
     * Обязательное целочисленное значение. Должно быть положительным (логическое ограничение, не выражено в аннотациях).
     * Используется при расчёте заработной платы на основе отработанных часов.
     * 
     */
    @Column(name = "HourlyRate", nullable = false)
    private int hourlyRate;

    /**
     * Нормативная продолжительность рабочего дня (в часах).
     * <p>
     * Обязательное целочисленное значение. Определяет, сколько часов в день сотрудник должен отработать
     * по графику (например, 8 для стандартного рабочего дня).
     * Может использоваться для расчёта переработок или недоработок.
     * 
     */
    @Column(name = "WorkHoursPerDay", nullable = false)
    private int workHoursPerDay;

    /**
     * Роль сотрудника в системе.
     * <p>
     * Обязательное поле, хранится в виде строкового значения перечисления ({@link EnumType#STRING}).
     * Определяет права доступа и функциональные возможности (например, в интерфейсе или при генерации отчётов).
     * 
     *
     * @see Role
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}