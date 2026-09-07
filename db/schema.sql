-- NexaFlow Database Schema (PostgreSQL)
-- Note: If you let Hibernate manage the schema (spring.jpa.hibernate.ddl-auto=update),
-- you do NOT need to run this manually - Spring Boot will create these tables for you
-- on first run. This script is provided for reference / manual setup.
--
-- Unlike MySQL, PostgreSQL cannot create a database via a plain SQL script run
-- inside psql connected to another DB. Create it first, e.g. from the terminal:
--   createdb nexaflow_db
-- or from psql:
--   CREATE DATABASE nexaflow_db;
-- Then connect to it (\c nexaflow_db) before running the rest of this file.

CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Hibernate maps @Enumerated(EnumType.STRING) to a plain VARCHAR column (not a
-- native Postgres ENUM type), so we use VARCHAR + CHECK here to match exactly
-- what Hibernate will generate.
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'MANAGER', 'EMPLOYEE')),
    manager_id BIGINT NULL,
    department_id BIGINT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_manager FOREIGN KEY (manager_id) REFERENCES users(id),
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    assigned_to BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'TODO' CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED')),
    priority VARCHAR(10) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id),
    CONSTRAINT fk_task_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS attendance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    check_in TIMESTAMP,
    check_out TIMESTAMP,
    date DATE NOT NULL,

    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    reviewed_by BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_leave_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_leave_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    performed_by VARCHAR(150),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
