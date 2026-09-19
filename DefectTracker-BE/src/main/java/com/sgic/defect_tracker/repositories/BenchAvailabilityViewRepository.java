package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.entities.BenchAvailabilityView;
import com.sgic.defect_tracker.entities.Defect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BenchAvailabilityViewRepository extends JpaRepository<BenchAvailabilityView, Long>, JpaSpecificationExecutor<BenchAvailabilityView> {

    Optional<BenchAvailabilityView> findByEmpId(Long empId);
    List<BenchAvailabilityView> findAll();

    Page<BenchAvailabilityView> findByEmpIdNot(Long empId, Pageable pageable);

//    List<BenchAllocation> findByProjectDetails_ProjectId(Long employeeId);


}
