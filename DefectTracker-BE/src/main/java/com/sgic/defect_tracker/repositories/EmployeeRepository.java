package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Employee;
import jakarta.persistence.criteria.From;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.hibernate.metamodel.mapping.WhereRestrictable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    //Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmailIgnoreCase(String email);

//forget password
    Optional<Employee> findByEmail(String email);

    //Optional<Employee> findByEmail(String email);
    //reset password
    Optional<Employee> findByForgetPasswordToken(String token);

    // password expiry token
    @Modifying
    @Query("""
    UPDATE Employee e
    SET e.forgetPasswordToken = null,
        e.forgetPasswordTokenExpiry = null
    WHERE e.forgetPasswordTokenExpiry IS NOT NULL
      AND e.forgetPasswordTokenExpiry < :currentTime
""")
    int clearExpiredForgotPasswordTokens(
            @Param("currentTime") LocalDateTime currentTime
    );


    boolean existsByWhatsappNumber(String whatsappNumber);

    boolean existsByWhatsappNumberAndEmpIdNot(
            String whatsappNumber,
            Long empId
    );

    List<Employee> findByDesignation_DesignationId(Long designationId);
    List<Employee> findByRole_RoleId(Long roleId);


    @Override
    Optional<Employee> findById(Long id);

   // <Employee> findByEmployee_empId(Long id);


    @Query(value = """
SELECT e.*
FROM employee e
LEFT JOIN designation d
ON d.designation_id = e.designation_id

WHERE

e.employee_id <> 1

AND

(
    :keyword IS NULL
    OR e.first_name ILIKE CONCAT('%', :keyword, '%')
    OR e.last_name ILIKE CONCAT('%', :keyword, '%')
    OR e.email ILIKE CONCAT('%', :keyword, '%')
    OR e.whatsapp_number ILIKE CONCAT('%', :keyword, '%')
    OR CAST(e.emp_id AS TEXT) ILIKE CONCAT('%', :keyword, '%')
    OR CONCAT('EMP', LPAD(CAST(e.emp_id AS TEXT), 4, '0'))
       ILIKE CONCAT('%', :keyword, '%')
)

AND
 
(
 :gender IS NULL
 OR LOWER(e.gender)=LOWER(:gender)
)

AND

(
 :status IS NULL
 OR CAST(e.is_active AS text)=:status
)

AND

(
 :designation IS NULL
 OR LOWER(d.designation_name)=LOWER(:designation)
)

""", nativeQuery = true)
    List<Employee> filterEmployees(
            @Param("keyword") String keyword,
            @Param("gender") String gender,
            @Param("status") String status,
            @Param("designation") String designation
    );

    // for import defects
    //Employee findByFirstNameIgnoreCase(String firstName);
    Optional<Employee> findByFirstNameIgnoreCase(String firstName);
    Page<Employee> findByEmpIdNot(Long empId, Pageable pageable);
}