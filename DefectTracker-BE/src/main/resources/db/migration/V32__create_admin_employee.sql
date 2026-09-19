-- Enable pgcrypto for BCrypt password encoding
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- Create ADMIN designation with ID = 1
INSERT INTO designation (
    designation_id,
    created_at,
    updated_at,
    designation_name
)
VALUES (
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'ADMIN'
);


-- Synchronize designation ID sequence
SELECT setval(
    pg_get_serial_sequence('designation', 'designation_id'),
    (SELECT MAX(designation_id) FROM designation)
);


-- Create initial ADMIN employee with ID = 1
INSERT INTO employee (
    emp_id,
    created_at,
    updated_at,
    email,
    first_name,
    gender,
    is_active,
    join_date,
    last_name,
    password,
    whatsapp_number,
    designation_id,
    role_role_id,
    login_type
)
VALUES (
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'admin@sgic.com',
    'Admin',
    'Male',
    TRUE,
    CURRENT_DATE,
    'SGIC',
    crypt('Admin@123', gen_salt('bf', 10)),
    '0700000000',
    1,
    NULL,
    'ADMIN'
);


-- Synchronize employee ID sequence
SELECT setval(
    pg_get_serial_sequence('employee', 'emp_id'),
    (SELECT MAX(emp_id) FROM employee)
);