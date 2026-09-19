package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.UpdateKlocRequest;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.ProjectKlocService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EndpointBundle.PROJECT)
public class ProjectKlocController {

    @Autowired
    private ProjectKlocService projectKlocService;

    @GetMapping("/{id}/kloc")
    public ResponseEntity<ResponseWrapper<Double>> getKloc(
            @PathVariable Long id) {

        Double kloc = projectKlocService.getKloc(id);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "KLOC retrieved successfully",
                        kloc
                )
        );
    }

    @PostMapping("/{id}/kloc")
    public ResponseEntity<ResponseWrapper<Double>> createKloc(
            @PathVariable Long id,
            @RequestBody UpdateKlocRequest request) {

        Double createdKloc =
                projectKlocService.createKloc(id, request.getKloc());

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.CREATED.getCode(),
                        "KLOC created successfully",
                        createdKloc
                )
        );
    }

    @PatchMapping("/{id}/kloc")
    public ResponseEntity<ResponseWrapper<Double>> updateKloc(
            @PathVariable Long id,
            @RequestBody UpdateKlocRequest request) {

        Double updatedKloc =
                projectKlocService.updateKloc(id, request.getKloc());

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.UPDATED.getCode(),
                        "KLOC updated successfully",
                        updatedKloc
                )
        );
    }

    @DeleteMapping("/{id}/kloc")
    public ResponseEntity<ResponseWrapper<Void>> deleteKloc(
            @PathVariable Long id) {

        projectKlocService.deleteKloc(id);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.DELETED.getCode(),
                        "KLOC deleted successfully",
                        null
                )
        );
    }
}