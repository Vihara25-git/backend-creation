CREATE OR REPLACE VIEW bench_availability_view AS
SELECT

    e.emp_id AS employee_id,
    CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
    d.designation_name AS designation_name,

    COALESCE(
            SUM(
                    CASE
                        WHEN ba.start_date <= CURRENT_TIMESTAMP
                            AND ba.end_date >= CURRENT_TIMESTAMP
                            THEN ba.availability
                        ELSE 0
                        END
            ),
            0
    ) AS total_allocated_percentage,

    GREATEST(
            0,
            100 - COALESCE(
                    SUM(
                            CASE
                                WHEN ba.start_date <= CURRENT_TIMESTAMP
                                    AND ba.end_date >= CURRENT_TIMESTAMP
                                    THEN ba.availability
                                ELSE 0
                                END
                    ),
                    0
                  )
    ) AS available_percentage,

    CASE
        WHEN COUNT(
                     CASE
                         WHEN ba.start_date <= CURRENT_TIMESTAMP
                             AND ba.end_date >= CURRENT_TIMESTAMP
                             THEN 1
                         END
             ) > 0
            THEN MAX(
                CASE
                    WHEN ba.start_date <= CURRENT_TIMESTAMP
                        AND ba.end_date >= CURRENT_TIMESTAMP
                        THEN ba.end_date
                    END
                 )
        ELSE NULL
        END AS available_from,

    STRING_AGG(
        DISTINCT
        CASE
            WHEN ba.start_date <= CURRENT_TIMESTAMP
             AND ba.end_date >= CURRENT_TIMESTAMP
            THEN pd.project_name
        END,
            ', '
    ) AS current_projects

FROM employee e
         LEFT JOIN designation d
                   ON d.designation_id = e.designation_id
         LEFT JOIN bench_allocation ba
                   ON ba.emp_id = e.emp_id
         LEFT JOIN project_details pd
                   ON pd.project_id = ba.project_id


GROUP BY
    e.emp_id,
    e.first_name,
    e.last_name,
    d.designation_name;