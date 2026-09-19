ALTER TABLE bench_allocation
DROP CONSTRAINT IF EXISTS fk_bench_employee;

ALTER TABLE bench_allocation
ADD CONSTRAINT fk_bench_employee
FOREIGN KEY (emp_id)
REFERENCES employee(emp_id)
ON DELETE SET NULL;

