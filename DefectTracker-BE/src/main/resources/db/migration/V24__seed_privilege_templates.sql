INSERT INTO privilege_template (type, sub_type, description, created_at, updated_at)
SELECT v.type, v.sub_type, v.description, NOW(), NOW()
FROM (VALUES

          -- =====================================================
          -- DEFECT
          -- =====================================================
          ('Defect', 'READ',   'View defects'),
          ('Defect', 'CREATE', 'Create defects'),
          ('Defect', 'UPDATE', 'Update defects'),
          ('Defect', 'DELETE', 'Delete defects'),

          -- =====================================================
          -- PROJECT
          -- =====================================================
          ('Project', 'READ',   'View projects'),
          ('Project', 'CREATE', 'Create projects'),
          ('Project', 'UPDATE', 'Update projects'),
          ('Project', 'DELETE', 'Delete projects'),

          -- =====================================================
          -- MODULE
          -- =====================================================
          ('Module', 'READ',   'View modules'),
          ('Module', 'CREATE', 'Create modules'),
          ('Module', 'UPDATE', 'Update modules'),
          ('Module', 'DELETE', 'Delete modules'),

          -- =====================================================
          -- SUB MODULE
          -- =====================================================
          ('SubModule', 'READ',   'View sub modules'),
          ('SubModule', 'CREATE', 'Create sub modules'),
          ('SubModule', 'UPDATE', 'Update sub modules'),
          ('SubModule', 'DELETE', 'Delete sub modules'),

          -- =====================================================
          -- RELEASE
          -- =====================================================
          ('Release', 'READ',   'View releases'),
          ('Release', 'CREATE', 'Create releases'),
          ('Release', 'UPDATE', 'Update releases'),
          ('Release', 'DELETE', 'Delete releases'),

          -- =====================================================
          -- DEFECT TYPE
          -- =====================================================
          ('DefectType', 'READ',   'View defect types'),
          ('DefectType', 'CREATE', 'Create defect types'),
          ('DefectType', 'UPDATE', 'Update defect types'),
          ('DefectType', 'DELETE', 'Delete defect types'),

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
          -- STATUS
          -- =====================================================
          ('Status', 'READ',   'View status types'),
          ('Status', 'CREATE', 'Create status types'),
          ('Status', 'UPDATE', 'Update status types'),
          ('Status', 'DELETE', 'Delete status types'),

          -- =====================================================
          -- EMPLOYEE / USER
          -- =====================================================
          ('Employee', 'READ',   'View employees'),
          ('Employee', 'CREATE', 'Create employees'),
          ('Employee', 'UPDATE', 'Update employees'),
          ('Employee', 'DELETE', 'Delete employees'),

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
          ('Permission', 'READ',   'View permissions'),
          ('Permission', 'CREATE', 'Create permissions'),
          ('Permission', 'UPDATE', 'Update permissions'),
          ('Permission', 'DELETE', 'Delete permissions')

     ) AS v(type, sub_type, description)
WHERE NOT EXISTS (
    SELECT 1
    FROM privilege_template p
    WHERE p.type = v.type
      AND p.sub_type = v.sub_type
);