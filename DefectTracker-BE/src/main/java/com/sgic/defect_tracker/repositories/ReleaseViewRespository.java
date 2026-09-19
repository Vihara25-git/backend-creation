package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.ReleaseView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface ReleaseViewRespository extends JpaRepository<ReleaseView, Long> {

}
