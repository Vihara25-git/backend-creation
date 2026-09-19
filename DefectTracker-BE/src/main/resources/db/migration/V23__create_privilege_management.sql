CREATE TABLE IF NOT EXISTS privilege_template (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(60) NOT NULL,
    sub_type VARCHAR(60) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_privilege_type_sub_type
        UNIQUE (type, sub_type)
);


CREATE TABLE IF NOT EXISTS role_privilege (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_role_template
        UNIQUE (role_id, template_id),

    CONSTRAINT fk_role_privilege_role
        FOREIGN KEY (role_id)
        REFERENCES role(role_id),

    CONSTRAINT fk_role_privilege_template
        FOREIGN KEY (template_id)
        REFERENCES privilege_template(id)
);


CREATE TABLE IF NOT EXISTS employee_privilege (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_employee_template
        UNIQUE (employee_id, template_id),

    CONSTRAINT fk_employee_privilege_template
        FOREIGN KEY (template_id)
        REFERENCES privilege_template(id)
);
