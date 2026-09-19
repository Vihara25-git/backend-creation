-- =====================================================
-- ADD PRIVILEGE TEMPLATES
-- =====================================================

INSERT INTO privilege_template (type, sub_type, description, created_at, updated_at)
SELECT v.type, v.sub_type, v.description, NOW(), NOW()
FROM (VALUES

          -- =====================================================
          -- DESIGNATION
          -- =====================================================
          ('Designation', 'READ',   'View designations'),
          ('Designation', 'CREATE', 'Create designations'),
          ('Designation', 'UPDATE', 'Update designations'),
          ('Designation', 'DELETE', 'Delete designations'),

          -- =====================================================
          -- ROLE
          -- =====================================================
          ('Role', 'READ',   'View roles'),
          ('Role', 'CREATE', 'Create roles'),
          ('Role', 'UPDATE', 'Update roles'),
          ('Role', 'DELETE', 'Delete roles'),

          -- =====================================================
          -- PERMISSION
          -- =====================================================
          ('Permission', 'READ', 'View permissions'),

          -- =====================================================
          -- ROLE PERMISSION
          -- =====================================================
          ('RolePermission', 'ASSIGN', 'Assign permissions to roles'),
          ('RolePermission', 'READ',   'View role permissions'),

          -- =====================================================
          -- EMPLOYEE PERMISSION
          -- =====================================================
          ('EmployeePermission', 'ASSIGN', 'Assign permissions to employees'),
          ('EmployeePermission', 'READ',   'View employee permissions'),

          -- =====================================================
          -- DEFECT TYPE
          -- =====================================================
          ('DefectType', 'READ',   'View defect types'),
          ('DefectType', 'CREATE', 'Create defect types'),
          ('DefectType', 'UPDATE', 'Update defect types'),
          ('DefectType', 'DELETE', 'Delete defect types'),

          -- =====================================================
          -- RELEASE TYPE
          -- =====================================================
          ('ReleaseType', 'READ',   'View release types'),
          ('ReleaseType', 'CREATE', 'Create release types'),
          ('ReleaseType', 'UPDATE', 'Update release types'),
          ('ReleaseType', 'DELETE', 'Delete release types'),

          -- =====================================================
          -- SEVERITY
          -- =====================================================
          ('Severity', 'READ',   'View severity levels'),
          ('Severity', 'CREATE', 'Create severity levels'),
          ('Severity', 'UPDATE', 'Update severity levels'),
          ('Severity', 'DELETE', 'Delete severity levels'),

          -- =====================================================
          -- PRIORITY
          -- =====================================================
          ('Priority', 'READ',   'View priorities'),
          ('Priority', 'CREATE', 'Create priorities'),
          ('Priority', 'UPDATE', 'Update priorities'),
          ('Priority', 'DELETE', 'Delete priorities'),

          -- =====================================================
          -- STATUS TYPE
          -- =====================================================
          ('Status', 'READ',   'View status types'),
          ('Status', 'CREATE', 'Create status types'),
          ('Status', 'UPDATE', 'Update status types'),
          ('Status', 'DELETE', 'Delete status types'),

          -- =====================================================
          -- WORKFLOW
          -- =====================================================
          ('Workflow', 'CREATE', 'Create workflows'),
          ('Workflow', 'READ',   'View workflows'),

          -- =====================================================
          -- EMAIL CONFIGURATION
          -- =====================================================
          ('EmailConfig', 'CREATE', 'Create email configurations'),
          ('EmailConfig', 'UPDATE', 'Update email configurations'),
          ('EmailConfig', 'READ',   'View email configurations'),
          ('EmailConfig', 'DELETE', 'Delete email configurations'),
          ('EmailConfig', 'CHANGE', 'Change email configuration status'),

          -- =====================================================
          -- POINT SETUP
          -- =====================================================
          ('PointSetup', 'READ',   'View point setup'),
          ('PointSetup', 'UPDATE', 'Update point setup'),

          -- =====================================================
          -- ROLE EMAIL RECIPIENT
          -- =====================================================
          ('RoleEmailRecipient', 'ASSIGN', 'Assign role email recipients'),
          ('RoleEmailRecipient', 'READ',   'View role email recipients'),

          -- =====================================================
          -- EMPLOYEE EMAIL RECIPIENT
          -- =====================================================
          ('EmployeeEmailRecipient', 'ASSIGN', 'Assign employee email recipients'),
          ('EmployeeEmailRecipient', 'READ',   'View employee email recipients'),

          -- =====================================================
          -- EMAIL TEMPLATE
          -- =====================================================
          ('EmailTemplate', 'UPDATE', 'Update email templates'),
          ('EmailTemplate', 'READ',   'View email templates'),

          -- =====================================================
          -- EMPLOYEE
          -- =====================================================
          ('Employee', 'CREATE',        'Create employees'),
          ('Employee', 'UPDATE',        'Update employees'),
          ('Employee', 'READ',          'View employees'),
          ('Employee', 'DELETE',        'Delete employees'),
          ('Employee', 'STATUS_UPDATE', 'Update employee status'),

          -- =====================================================
          -- BENCH
          -- =====================================================
          ('Bench', 'READ', 'View bench employees'),

          -- =====================================================
          -- PROJECT
          -- =====================================================
          ('Project', 'CREATE', 'Create projects'),
          ('Project', 'UPDATE', 'Update projects'),
          ('Project', 'READ',   'View projects'),
          ('Project', 'DELETE', 'Delete projects'),

          -- =====================================================
          -- MODULE
          -- =====================================================
          ('Module', 'CREATE', 'Create modules'),
          ('Module', 'UPDATE', 'Update modules'),
          ('Module', 'READ',   'View modules'),
          ('Module', 'DELETE', 'Delete modules'),

          -- =====================================================
          -- SUB MODULE
          -- =====================================================
          ('SubModule', 'CREATE', 'Create sub modules'),
          ('SubModule', 'UPDATE', 'Update sub modules'),
          ('SubModule', 'READ',   'View sub modules'),
          ('SubModule', 'DELETE', 'Delete sub modules'),

          -- =====================================================
          -- PROJECT ALLOCATION
          -- =====================================================
          ('ProjectAllocation', 'ASSIGN',    'Assign employees to projects'),
          ('ProjectAllocation', 'READ',      'View project allocations'),
          ('ProjectAllocation', 'EXTEND',    'Extend project allocations'),
          ('ProjectAllocation', 'DEALLOCATE', 'Deallocate employees from projects'),

          -- =====================================================
          -- TEST CASE
          -- =====================================================
          ('TestCase', 'CREATE', 'Create test cases'),
          ('TestCase', 'UPDATE', 'Update test cases'),
          ('TestCase', 'READ',   'View test cases'),
          ('TestCase', 'DELETE', 'Delete test cases'),

          -- =====================================================
          -- RELEASE
          -- =====================================================
          ('Release', 'CREATE',        'Create releases'),
          ('Release', 'UPDATE',        'Update releases'),
          ('Release', 'READ',          'View releases'),
          ('Release', 'DELETE',        'Delete releases'),
          ('Release', 'STATUS_CHANGE', 'Change release status'),

          -- =====================================================
          -- DEFECT
          -- =====================================================
          ('Defect', 'CREATE',               'Create defects'),
          ('Defect', 'UPDATE',               'Update defects'),
          ('Defect', 'READ',                 'View defects'),
          ('Defect', 'DELETE',               'Delete defects'),
          ('Defect', 'ASSIGN_DEVELOPER',     'Assign developers to defects'),
          ('Defect', 'STATUS_CHANGE',        'Change defect status'),
          ('Defect', 'READ_STATUS_HISTORY',  'View defect status history'),

          -- =====================================================
          -- DEFECT COMMENT
          -- =====================================================
          ('DefectComment', 'CREATE', 'Create defect comments'),
          ('DefectComment', 'READ',   'View defect comments')

     ) AS v(type, sub_type, description)

WHERE NOT EXISTS (
    SELECT 1
    FROM privilege_template p
    WHERE p.type = v.type
      AND p.sub_type = v.sub_type
);