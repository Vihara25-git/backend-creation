-- ============================================================
-- V31 : Create Email Config Table
-- ============================================================

CREATE TABLE IF NOT EXISTS email_config (
                                            id BIGSERIAL PRIMARY KEY,

                                            name VARCHAR(255) NOT NULL,

    from_email VARCHAR(255) NOT NULL,

    host VARCHAR(255),

    port INTEGER,

    status BOOLEAN NOT NULL DEFAULT FALSE,

    smtp_username VARCHAR(255),

    smtp_password VARCHAR(255),

    from_name VARCHAR(255),

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255)
    );


-- ============================================================
-- INSERT EMAIL CONFIG
-- ============================================================

INSERT INTO email_config
(
    name,
    from_email,
    host,
    port,
    status,
    smtp_username,
    smtp_password,
    from_name,
    created_at,
    updated_at
)
VALUES
    (
        'SMTP',
        'javascriptjava190@gmail.com',
        'smtp.gmail.com',
        587,
        TRUE,
        'javascriptjava190@gmail.com',
        'ugqr pbsl goui ufuc',
        'Defect Tracking System',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),(
        'SMTP SERVER',
        'gobitha20@gmail.com',
        'smtp.gmail.com',
        587,
        FALSE,
        'gobitha20@gmail.com',
        'okoh ickz vfaa ttro',
        'Defect Tracking System New',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );