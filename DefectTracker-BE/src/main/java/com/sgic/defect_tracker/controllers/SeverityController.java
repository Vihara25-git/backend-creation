package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.SeverityRequestDTO;
import com.sgic.defect_tracker.dtos.response.SeverityResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.SeverityService;
import com.sgic.defect_tracker.utils.EndpointBundle;

import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping(EndpointBundle.Severity)

public class SeverityController {

    private final SeverityService severityService;

    public SeverityController(SeverityService severityService) {
        this.severityService = severityService;
    }

    @PostMapping()
    public ResponseEntity<ResponseWrapper<SeverityResponseDTO>>createSeverity(@Valid @RequestBody SeverityRequestDTO severityRequestDto) {
        SeverityResponseDTO createSeverity = severityService.CreateSeverity(severityRequestDto);
        if (createSeverity != null) {
            ResponseWrapper<SeverityResponseDTO> response = new ResponseWrapper<>(
                    RestApiResponseStatusCodes.CREATED.getCode(),
                    ValidationMessages.SAVED_SUCCESSFULL,
                    null
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        else{
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(
                    RestApiResponseStatusCodes.NO_CONTENT.getCode(),
                    ValidationMessages.SAVE_FAILED,
                    null
            ));
        }
    }

    @GetMapping()
    public ResponseEntity <ResponseWrapper<Page<SeverityResponseDTO>>> getAllSeverity(Pageable pageable) {
        Page<SeverityResponseDTO> severities = severityService.getAllSeverity(pageable);
//        if (severities.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseWrapper<>(
//                    RestApiResponseStatusCodes.NOT_FOUND.getCode(),
//                    RestApiResponseStatusCodes.NOT_FOUND.getMessage(),
//                    severities
//            ));
//        }

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS.getCode(),
                ValidationMessages.RETRIEVED,
                severities
        ));
    }

    @DeleteMapping(EndpointBundle.ID)
    public ResponseEntity<ResponseWrapper<Void>> deleteSeverity(@PathVariable Long id){
        severityService.deleteSeverity(id);
        return ResponseEntity.ok(new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS.getCode(),
                ValidationMessages.DELETE_SUCCESS,
                null
        ));
    }



    @PutMapping(EndpointBundle.ID)
    public ResponseEntity<ResponseWrapper<SeverityResponseDTO>> updateSeverity(
            @PathVariable Long id,
            @Valid @RequestBody SeverityRequestDTO severityRequestDTO) {

        SeverityResponseDTO updatedSeverity = severityService.updateSeverityById(id, severityRequestDTO);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS.getCode(),
                ValidationMessages.UPDATE_SUCCESSFULL,
                updatedSeverity
        ));
    }
}



