package com.sgic.defect_tracker.dtos.response;

import lombok.Data;
// for import defects
@Data
public class ImportDefectResponseDTO {

    private int total;
    private int imported;
    private int failed;
    private int skipped;
}