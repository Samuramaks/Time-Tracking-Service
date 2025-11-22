-- Создаём ENUM для ролей (соответствует Java enum Role)
CREATE TYPE IF NOT EXISTS role_enum AS ENUM ('EMPLOYEE', 'HR');

-- Таблица сотрудников
CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "FIO" VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    "HourlyRate" INTEGER NOT NULL,
    "WorkHoursPerDay" INTEGER NOT NULL,
    role role_enum NOT NULL
);

-- Таблица записей времени
CREATE TABLE IF NOT EXISTS time_entry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    register TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    check_out TIMESTAMP WITHOUT TIME ZONE,
    is_manual BOOLEAN NOT NULL DEFAULT FALSE
);

-- Индексы для производительности
CREATE INDEX IF NOT EXISTS idx_time_entry_employee_id ON time_entry(employee_id);
CREATE INDEX IF NOT EXISTS idx_time_entry_register ON time_entry(register);
CREATE INDEX IF NOT EXISTS idx_time_entry_check_out ON time_entry(check_out);