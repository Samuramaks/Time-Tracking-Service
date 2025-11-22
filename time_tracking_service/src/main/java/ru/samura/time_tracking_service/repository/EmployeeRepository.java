package ru.samura.time_tracking_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.samura.time_tracking_service.entity.Employee;

/**
 * Репозиторий для управления сущностями {@link Employee} в системе учёта рабочего времени.
 * <p>
 * Предоставляет стандартные CRUD-операции на основе Spring Data JPA, а также позволяет
 * легко расширять функциональность через объявление методов по соглашению об именах
 * 
 * @see Employee
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

}