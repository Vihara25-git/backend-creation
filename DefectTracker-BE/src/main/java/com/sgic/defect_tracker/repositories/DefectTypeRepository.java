package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.DefectType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DefectTypeRepository extends JpaRepository<DefectType,Long> {

//    @Query("""
//    select *
//    from DefectType
//    where defectTypeId=:id
//
//
//"""
//
//    )
//    DefectTypeResponseDto defectupdate(Long id, DefectTypeRequestDto defect);

    // added jst for import defects
    //DefectType findByDefectTypeNameIgnoreCase(String defectTypeName);
    Optional<DefectType> findByDefectTypeNameIgnoreCase(
            String defectTypeName
    );

}
