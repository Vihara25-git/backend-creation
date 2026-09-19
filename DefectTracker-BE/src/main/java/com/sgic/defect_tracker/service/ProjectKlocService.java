package com.sgic.defect_tracker.service;

public interface ProjectKlocService {

    Double getKloc(Long projectId);

    Double createKloc(Long projectId, Double kloc);

    Double updateKloc(Long projectId, Double kloc);

    void deleteKloc(Long projectId);
}

