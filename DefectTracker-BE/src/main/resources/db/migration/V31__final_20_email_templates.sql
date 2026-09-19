
-- ============================================================
-- V29 : Create Email Template Table with Final 17 Email Templates
-- ============================================================

DROP TABLE IF EXISTS email_template CASCADE;


-- ============================================================
-- CREATE EMAIL TEMPLATE TABLE
-- ============================================================

CREATE TABLE email_template (
    template_id BIGSERIAL PRIMARY KEY,

    email_notification_type VARCHAR(100) NOT NULL,

    subject VARCHAR(255) NOT NULL,

    body TEXT NOT NULL,

    status BOOLEAN NOT NULL DEFAULT FALSE,

  default_body TEXT,
     default_subject VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by VARCHAR(255)
);


-- ============================================================
-- INSERT EMAIL TEMPLATES
-- ============================================================

-- ============================================================
-- 1. DEFECT_ASSIGNED
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'DEFECT_ASSIGNED',
    'Defect Assigned - {{defectId}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Defect Assigned</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Defect Assigned</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Defect Assigned to You</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">A defect has been assigned to you. Please review the details below and proceed with the required investigation or resolution.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{projectName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Defect ID</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{defectId}}</strong></td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Description</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{briefDescription}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Status</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{statusType}}</strong></td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please log in to the system to review the complete defect details and continue the assigned work.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. EMPLOYEE_CREATED
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'EMPLOYEE_CREATED',
    'Employee Account Created',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Employee Account Created</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Employee Account Created</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Welcome to the System</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">Your employee account has been created successfully. Please use the credentials below to access the system.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Email Address</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{email}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Temporary Password</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{password}}</strong></td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>For security, change your temporary password immediately after your first login.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 3. EMPLOYEE_EMAIL_UPDATED
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'EMPLOYEE_EMAIL_UPDATED',
    'Employee Account Details Updated',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Account Details Updated</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Account Details Updated</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Your Login Details Have Changed</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">Your employee account details have been updated successfully. Your new login credentials are provided below.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">New Email Address</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{email}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Temporary Password</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{password}}</strong></td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please use the new credentials for your next login and change the temporary password immediately.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 4. DEFECT_UPDATED
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'DEFECT_UPDATED',
    'Defect Status Changed - {{defectId}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Defect Status Updated</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Defect Status Updated</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Defect Status Has Changed</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">The status of a defect has been updated in the Defect Tracking System.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{projectName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Defect ID</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{defectId}}</strong></td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Previous Status</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{fromStatus}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">New Status</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{toStatus}}</strong></td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please log in to the system to review the latest defect details.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 5. DEFECT_REASSIGNED
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'DEFECT_REASSIGNED',
    'Defect Reassigned - {{defectId}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Defect Reassigned</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Defect Reassigned</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Defect Reassigned to You</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">A defect has been reassigned to you. Please review the updated assignment details.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{projectName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Defect ID</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{defectId}}</strong></td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Description</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{briefDescription}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Status</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{statusType}}</strong></td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Previous Assignee</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{assignTo}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Reassigned To</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{firstName}} {{lastName}}</strong></td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please log in to review the defect and continue the assigned work.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 6. MODULE_ALLOCATION
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'MODULE_ALLOCATION',
    'Module Assignment - {{moduleName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Module Assignment</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Module Assignment</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Module Assigned to You</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">You have been assigned to the following module in the Defect Tracking System.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{projectName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Module Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{moduleName}}</strong></td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please review the module details and begin the assigned QA activities.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 7. SUBMODULE_ALLOCATION
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'SUBMODULE_ALLOCATION',
    'Submodule Assignment - {{subModuleName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Submodule Assignment</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Submodule Assignment</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Submodule Assigned to You</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">You have been assigned to the following submodule in the Defect Tracking System.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{projectName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Module Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{moduleName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Submodule Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{subModuleName}}</strong></td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please review the submodule details and begin the assigned development work.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 8. EMPLOYEE_DEACTIVATED
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'EMPLOYEE_DEACTIVATED',
    'Account Deactivated',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Account Deactivated</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Account Deactivated</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Account Access Disabled</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">Your Defect Tracking System account has been deactivated.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Employee Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{employeeName}}</td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>You will no longer be able to access the system using your current credentials. Please contact your administrator if you believe this was done in error.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 9. PROJECT_ALLOCATION
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'PROJECT_ALLOCATION',
    'Project Allocation - {{projectName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Project Allocation</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Project Allocation</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Project Allocation Confirmed</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">You have been allocated to the following project in the Defect Tracking System.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{projectName}}</strong></td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Role</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{roleName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Start Date</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{startDate}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">End Date</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{endDate}}</td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please review your project responsibilities and allocation period in the system.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 10. PROJECT_CREATED
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'PROJECT_CREATED',
    'Project Created - {{projectName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Project Created</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Project Created</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">A New Project Is Available</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{projectManager}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">A new project has been created and assigned to the Project Manager.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{projectName}}</strong></td></tr>
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Manager</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{projectManager}}</td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please log in to the system to review the project details.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 11. PROJECT_DEALLOCATION
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'PROJECT_DEALLOCATION',
    'Project Deallocation - {{projectName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Project Deallocation</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f3f6fa;width:100%;">
<tr><td align="center" style="padding:32px 16px;">
<table role="presentation" width="680" cellspacing="0" cellpadding="0" style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<tr><td style="height:5px;background:#2563eb;"></td></tr>
<tr><td style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Project Deallocation</div>
</td></tr>
<tr><td style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Project Allocation Removed</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">Your allocation to the following project has been removed.</p>
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">
<tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{projectName}}</strong></td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Role</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{roleName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">End Date</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{endDate}}</td></tr>
</table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please review your remaining project assignments in the system.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</td></tr>
<tr><td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</td></tr>
</table>
</td></tr>
</table>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 12. CREATE FINAL TEMPLATE
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
'MODULE_DEALLOCATION',
    'QA Module Deallocation - {{moduleName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>QA Module Deallocation</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<div style="max-width:680px;margin:32px auto;padding:0 16px;">
<div style="background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<div style="height:5px;background:#2563eb;"></div>
<div style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">QA Module Deallocation</div>
</div>
<div style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">QA Module Allocation Removed</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">Your QA allocation for the following module has been removed.</p>
<table style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;"><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{projectName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Module Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{moduleName}}</strong></td></tr></table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>You are no longer assigned as QA for this module. Please review your current assignments.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</div>
<div style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</div>
</div>
</div>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 13. CREATE FINAL TEMPLATE
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
'SUBMODULE_DEALLOCATION',
    'Developer Submodule Deallocation - {{subModuleName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Developer Deallocation</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<div style="max-width:680px;margin:32px auto;padding:0 16px;">
<div style="background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<div style="height:5px;background:#2563eb;"></div>
<div style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">Developer Deallocation</div>
</div>
<div style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Submodule Allocation Removed</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">Your developer allocation for the following submodule has been removed.</p>
<table style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;"><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{projectName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Module Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{moduleName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Submodule Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{subModuleName}}</strong></td></tr></table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>You are no longer assigned as the developer for this submodule. Please review your remaining assignments.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</div>
<div style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</div>
</div>
</div>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 14. CREATE FINAL TEMPLATE
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
'QA_TESTCASE_ALLOCATION',
    'QA Test Case Assignment - {{testCaseName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>QA Test Case Assignment</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<div style="max-width:680px;margin:32px auto;padding:0 16px;">
<div style="background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<div style="height:5px;background:#2563eb;"></div>
<div style="background:#1f2937;padding:30px 34px;color:#ffffff;">
<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">Defect Tracking System</div>
<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">QA Test Case Assignment</div>
</div>
<div style="padding:34px;">
<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">Test Case Assigned to You</div>
<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">A test case has been assigned to you for QA validation.</p>
<table style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;"><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Project Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{projectName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Module Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">{{moduleName}}</td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Test Case ID</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{testCaseId}}</strong></td></tr><tr><td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">Test Case Name</td><td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;"><strong>{{testCaseName}}</strong></td></tr></table>
<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">
<strong style="color:#1f2937;">Action Required</strong><br>Please log in to the system, review the test case details and complete the assigned QA validation.
</div>
<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">Regards,<br><strong style="color:#1f2937;">Defect Tracking System</strong></p>
</div>
<div style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">This is an automated notification from the Defect Tracking System.</div>
</div>
</div>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 15. PASSWORD_RESET
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'PASSWORD_RESET',
    'Password Reset Request',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Password Reset Request</title>
</head>

<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">

<div style="max-width:680px;margin:32px auto;padding:0 16px;">

<div style="background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;">

<div style="height:5px;background:#2563eb;"></div>

<div style="background:#1f2937;padding:30px 34px;color:#ffffff;">
    <div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">
        Defect Tracking System
    </div>

    <div style="font-size:25px;font-weight:700;margin-top:9px;">
        Password Reset Request
    </div>
</div>

<div style="padding:34px;">

    <div style="font-size:20px;font-weight:700;color:#1f2937;margin-bottom:18px;">
        Reset Your Account Password
    </div>

    <p style="font-size:14px;line-height:1.8;color:#5b6b7f;">
        Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,
    </p>

    <p style="font-size:14px;line-height:1.8;color:#5b6b7f;">
        We received a request to reset your password for the Defect Tracking System.
        Use the button below to create a new password.
    </p>

    <div style="text-align:center;margin:30px 0;">
        <a href="{{resetLink}}"
           style="display:inline-block;background:#6d8ce8;color:#ffffff;text-decoration:none;font-size:16px;font-weight:700;padding:16px 38px;border-radius:8px;">
            Reset Password
        </a>
    </div>

    <div style="margin-top:22px;padding:16px 18px;background:#fff8ed;border:1px solid #f4d7a1;border-left:4px solid #f97316;border-radius:7px;font-size:13px;line-height:1.7;color:#8a5a20;">
        <strong style="color:#7c4a16;">Important</strong><br>
        This password reset link is valid for <strong>10 minutes</strong> only.
        If you did not request a password reset, please ignore this email and your password will remain unchanged.
    </div>

    <p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">
        Regards,<br>
        <strong style="color:#1f2937;">Defect Tracking System</strong>
    </p>

</div>

<div style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;">
    This is an automated notification from the Defect Tracking System.
</div>

</div>
</div>

</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 16. PASSWORD_CHANGED
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'PASSWORD_CHANGED',
    'Password Changed Successfully',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Password Changed Successfully</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<div style="max-width:680px;margin:32px auto;padding:0 16px;">
<div style="background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<div style="height:5px;background:#2563eb;"></div>
<div style="background:#1f2937;padding:30px 34px;color:#ffffff;text-align:center;">
<div style="width:72px;height:72px;margin:0 auto 18px;background:#64748b;border-radius:50%;font-size:38px;line-height:72px;font-weight:700;color:#1f2937;">D</div>
<div style="font-size:28px;line-height:1.3;font-weight:700;">Password Changed<br>Successfully</div>
<div style="font-size:16px;line-height:1.6;color:#cbd5e1;margin-top:12px;">Your account password has been updated</div>
</div>
<div style="padding:34px;">
<p style="margin:0 0 24px;font-size:16px;line-height:1.8;color:#5b6b7f;">Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 24px;font-size:16px;line-height:1.8;color:#5b6b7f;">Your password for the Defect Tracking System has been changed successfully.</p>
<div style="margin-top:22px;padding:18px 20px;background:#fff7ed;border-left:4px solid #f97316;border-radius:8px;font-size:14px;line-height:1.7;color:#9a3412;">
<strong>If you did not perform this action,</strong><br>
please contact support or your system administrator immediately and secure your account.
</div>
</div>
<div style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:12px;line-height:1.6;text-align:center;">
© 2026 Defect Tracking System. All Rights Reserved.
</div>
</div>
</div>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 17. LOGIN_SUCCESSFUL
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'LOGIN_SUCCESSFUL',
    'Login Successful',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Login Successful</title>
</head>
<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">
<div style="max-width:680px;margin:32px auto;padding:0 16px;">
<div style="background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">
<div style="height:5px;background:#2563eb;"></div>
<div style="background:#1f2937;padding:30px 34px;color:#ffffff;text-align:center;">
<div style="width:72px;height:72px;margin:0 auto 18px;background:#16a34a;border-radius:50%;font-size:40px;line-height:72px;font-weight:700;color:#ffffff;">✓</div>
<div style="font-size:28px;line-height:1.3;font-weight:700;">Login Successful</div>
<div style="font-size:16px;line-height:1.6;color:#cbd5e1;margin-top:12px;">Your account was accessed successfully</div>
</div>
<div style="padding:34px;">
<p style="margin:0 0 24px;font-size:16px;line-height:1.8;color:#5b6b7f;">Dear <strong style="color:#1f2937;">{{employeeName}}</strong>,</p>
<p style="margin:0 0 24px;font-size:16px;line-height:1.8;color:#5b6b7f;">You have successfully logged into your account. Below are the details of your recent login activity.</p>
<div style="background:#ffffff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;">
<div style="padding:18px 20px;border-bottom:1px solid #e2e8f0;">
<div style="font-size:13px;letter-spacing:1px;color:#64748b;text-transform:uppercase;">Employee Name</div>
<div style="margin-top:8px;font-size:17px;font-weight:700;color:#1f2937;">{{employeeName}}</div>
</div>
<div style="padding:18px 20px;">
<div style="font-size:13px;letter-spacing:1px;color:#64748b;text-transform:uppercase;">Login Time</div>
<div style="margin-top:8px;font-size:17px;font-weight:700;color:#1f2937;">{{loginTime}}</div>
</div>
</div>
</div>
</div>
</div>
</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 18. RESET_PASSWORD_SUCCESSFUL
-- ============================================================
INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'PASSWORD_RESET_SUCCESS',
    'Password Reset Successful',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Password Reset Successful</title>
</head>

<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">

<table role="presentation" width="100%" cellspacing="0" cellpadding="0"
       style="border-collapse:collapse;background:#f3f6fa;width:100%;">

<tr>
<td align="center" style="padding:32px 16px;">

<table role="presentation" width="680" cellspacing="0" cellpadding="0"
       style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">

<tr>
<td style="height:5px;background:#2563eb;"></td>
</tr>

<tr>
<td style="background:#1f2937;padding:30px 34px;color:#ffffff;">

<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">
Defect Tracking System
</div>

<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">
Password Reset Successful
</div>

</td>
</tr>

<tr>
<td style="padding:34px;">

<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">
Your Password Has Been Reset
</div>

<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">
Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,
</p>

<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">
Your password for the Defect Tracking System has been successfully reset.
You can now use your new password to log in to your account.
</p>

<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">

<strong style="color:#1f2937;">Security Notice</strong>
<br>

If you did not make this password change, please contact the system administrator immediately.

</div>

<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">
Regards,<br>
<strong style="color:#1f2937;">Defect Tracking System</strong>
</p>

</td>
</tr>

<tr>
<td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">
This is an automated notification from the Defect Tracking System.
</td>
</tr>

</table>

</td>
</tr>

</table>

</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 19. update project
-- ============================================================
INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'PROJECT_UPDATED',
    'Project Updated - {{projectName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Project Updated</title>
</head>

<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">

<table role="presentation" width="100%" cellspacing="0" cellpadding="0"
       style="border-collapse:collapse;background:#f3f6fa;width:100%;">

<tr>
<td align="center" style="padding:32px 16px;">

<table role="presentation" width="680" cellspacing="0" cellpadding="0"
       style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">

<tr>
<td style="height:5px;background:#2563eb;"></td>
</tr>

<tr>
<td style="background:#1f2937;padding:30px 34px;color:#ffffff;">

<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">
Defect Tracking System
</div>

<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">
Project Updated
</div>

</td>
</tr>

<tr>
<td style="padding:34px;">

<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">
Project Details Have Been Updated
</div>

<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">
Hello <strong style="color:#1f2937;">{{projectManager}}</strong>,
</p>

<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">
The details of the project below have been updated in the Defect Tracking System.
</p>

<table role="presentation" width="100%" cellspacing="0" cellpadding="0"
       style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">

<tr>
<td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">
Project Name
</td>

<td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">
<strong>{{projectName}}</strong>
</td>
</tr>

<tr>
<td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">
Project Manager
</td>

<td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">
{{projectManager}}
</td>
</tr>

<tr>
<td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">
Project Status
</td>

<td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">
<strong>{{status}}</strong>
</td>
</tr>

<tr>
<td style="width:34%;padding:14px 16px;background:#f8fafc;font-size:13px;font-weight:700;color:#64748b;">
Updated At
</td>

<td style="padding:14px 16px;font-size:13px;color:#1f2937;">
{{updatedAt}}
</td>
</tr>

</table>

<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">

<strong style="color:#1f2937;">Action Required</strong><br>
Please log in to the system to review the updated project details.

</div>

<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">
Regards,<br>
<strong style="color:#1f2937;">Defect Tracking System</strong>
</p>

</td>
</tr>

<tr>
<td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">
This is an automated notification from the Defect Tracking System.
</td>
</tr>

</table>

</td>
</tr>

</table>

</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- 20. PROJECT_ALLOCATION_UPDATED
-- ============================================================

INSERT INTO email_template
(
    email_notification_type,
    subject,
    body,
    status,
    created_at,
    updated_at
)
VALUES
(
    'PROJECT_ALLOCATION_UPDATED',
    'Project Allocation Updated - {{projectName}}',
    $html$
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Project Allocation Updated</title>
</head>

<body style="margin:0;padding:0;background:#f3f6fa;font-family:'Segoe UI',Arial,Helvetica,sans-serif;color:#334155;">

<table role="presentation" width="100%" cellspacing="0" cellpadding="0"
       style="border-collapse:collapse;background:#f3f6fa;width:100%;">

<tr>
<td align="center" style="padding:32px 16px;">

<table role="presentation" width="680" cellspacing="0" cellpadding="0"
       style="width:100%;max-width:680px;border-collapse:collapse;background:#ffffff;border:1px solid #d8e1ec;border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.07);">

<tr>
<td style="height:5px;background:#2563eb;"></td>
</tr>

<tr>
<td style="background:#1f2937;padding:30px 34px;color:#ffffff;">

<div style="font-size:11px;letter-spacing:1.6px;font-weight:700;color:#bfdbfe;text-transform:uppercase;">
Defect Tracking System
</div>

<div style="font-size:25px;line-height:1.3;font-weight:700;margin-top:9px;">
Project Allocation Updated
</div>

</td>
</tr>

<tr>
<td style="padding:34px;">

<div style="font-size:20px;line-height:1.4;font-weight:700;color:#1f2937;margin-bottom:18px;">
Project Allocation Has Been Updated
</div>

<p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#5b6b7f;">
Hello <strong style="color:#1f2937;">{{employeeName}}</strong>,
</p>

<p style="margin:0 0 22px;font-size:14px;line-height:1.8;color:#5b6b7f;">
Your project allocation details have been updated in the Defect Tracking System.
</p>

<table role="presentation" width="100%" cellspacing="0" cellpadding="0"
       style="width:100%;border-collapse:collapse;border:1px solid #d8e1ec;border-radius:8px;overflow:hidden;">

<tr>
<td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">
Project Name
</td>

<td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">
<strong>{{projectName}}</strong>
</td>
</tr>

<tr>
<td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">
Role
</td>

<td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">
{{roleName}}
</td>
</tr>

<tr>
<td style="width:34%;padding:14px 16px;background:#f8fafc;border-bottom:1px solid #e2e8f0;font-size:13px;font-weight:700;color:#64748b;">
Start Date
</td>

<td style="padding:14px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#1f2937;">
{{startDate}}
</td>
</tr>

<tr>
<td style="width:34%;padding:14px 16px;background:#f8fafc;font-size:13px;font-weight:700;color:#64748b;">
End Date
</td>

<td style="padding:14px 16px;font-size:13px;color:#1f2937;">
{{endDate}}
</td>
</tr>

</table>

<div style="margin-top:22px;padding:16px 18px;background:#f1f7ff;border:1px solid #cfe0f5;border-left:4px solid #3b82f6;border-radius:7px;font-size:13px;line-height:1.7;color:#526275;">

<strong style="color:#1f2937;">Action Required</strong><br>
Please review your updated project responsibilities and allocation period in the system.

</div>

<p style="margin:26px 0 0;font-size:14px;line-height:1.8;color:#5b6b7f;">
Regards,<br>
<strong style="color:#1f2937;">Defect Tracking System</strong>
</p>

</td>
</tr>

<tr>
<td style="background:#f8fafc;border-top:1px solid #d8e1ec;padding:18px 34px;color:#7b8794;font-size:11px;line-height:1.6;">
This is an automated notification from the Defect Tracking System.
</td>
</tr>

</table>

</td>
</tr>

</table>

</body>
</html>
$html$,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
-- ============================================================
-- COPY CURRENT VALUES INTO DEFAULT VALUES
-- ============================================================

UPDATE email_template
SET
    default_body = body,
    default_subject = subject;

-- ============================================================
-- MAKE DEFAULT COLUMNS REQUIRED
-- ============================================================

ALTER TABLE email_template
    ALTER COLUMN default_body SET NOT NULL;

ALTER TABLE email_template
    ALTER COLUMN default_subject SET NOT NULL;


-- ============================================================
-- VERIFICATION : FINAL 18
-- ============================================================

SELECT
    template_id,
    email_notification_type,
    subject,
    status
FROM email_template
ORDER BY template_id;

SELECT COUNT(*) AS total_templates
FROM email_template;
