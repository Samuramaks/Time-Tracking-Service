# 🕒 Time Tracking Service  
**Система учёта рабочего времени и расчёта заработной платы**  
> Production-ready Spring Boot backend для HR-отделов и тайм-менеджмента.

[![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=java&logoColor=white)](https://openjdk.org)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com)

---

## 🌟 Особенности

- ✅ **Полный жизненный цикл смены**: вход (`clock-in`), выход (`clock-out`), ручная коррекция HR  
- 📊 **Автоматический расчёт**: отработанных часов, нормы (20 дней × 8 ч), переработки, заработной платы  
- 🔐 **Гибкая RBAC-безопасность**:  
  - `EMPLOYEE` — только свои данные  
  - `HR` — полный доступ ко всем сотрудникам и отчётам  
- 🧪 **Тестирование**: 14 unit- и integration-тестов (Mockito, `@WebMvcTest`)  
- 📚 **Enterprise-документация**:  
  - Javadoc с описанием логики, ограничений и предупреждений  
  - Swagger UI — интерактивная спецификация API  
- 🐳 **Готов к деплою**: Docker + PostgreSQL, healthcheck, production-профиль  
- 🧠 **Архитектура**: чёткое разделение слоёв (Controller → Service → Repository), DTO, кеширование (`@Cacheable`)

--- 
## Сборка

# запустить в Docker (с PostgreSQL)
docker-compose up --build

---
## Структура проекта
src/
├── controller/       # REST API (TimeEntryController, ReportController)
├── service/          # Бизнес-логика (TimeEntryService, EmployeeService)
├── repository/       # JPA-репозитории (EmployeeRepository, TimeEntryRepository)
├── DTO/              # Data Transfer Objects (EmployeeDTO, TimeEntryDTO)
├── entity/           # JPA-сущности (Employee, TimeEntry, Role)
├── response/         # Ответы API (ClockInResponse, TotalPaymentEmployeeResponse)
├── custom_exception/ # Кастомные исключения (ActiveShiftTodayException и др.)
├── swagger/          # OpenAPI-конфигурация
└── resources/
    ├── application.yml    # Конфигурация Spring Boot
    └── data.sql           # Пример инициализации БД


---

## 📖 Документация

Javadoc - target/site/apidocs/index.html (после mvn javadoc:javadoc)

Swagger UI - http://localhost:8080/swagger-ui
Архитектура - Подробные комментарии в коде (например, в TimeEntryService.totalPaymentEmployee())

---

## Быстрый старт

### 1. Подготовка
Создайте файл `.env` в корне проекта:
```env
# PostgreSQL
POSTGRES_DB=time_tracking
POSTGRES_USER=user
POSTGRES_PASSWORD=user

# Spring Boot
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/time_tracking
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=user
API_SERVER_URL=http://localhost:8080
