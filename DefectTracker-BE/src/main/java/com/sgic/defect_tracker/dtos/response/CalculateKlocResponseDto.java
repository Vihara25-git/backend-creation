//package com.sgic.defect_tracker.dtos.response;
//
//public class CalculateKlocResponseDto {
//
//    private double backendLOC;
//    private double frontendLOC;
//    private double backendKLOC;
//    private double frontendKLOC;
//    private double totalKLOC;
//}

package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculateKlocResponseDto {

    private long backendLOC;

    private long frontendLOC;

    private double backendKLOC;

    private double frontendKLOC;

    private double totalKLOC;
}