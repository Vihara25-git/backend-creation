package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.PriorityRequestDto;
import com.sgic.defect_tracker.dtos.response.PriorityResponseDto;
import com.sgic.defect_tracker.entities.Priority;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.PriorityService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EndpointBundle.PRIORITY)
public class PriorityController {
    private final PriorityService priorityService;

    public PriorityController(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    @PutMapping(EndpointBundle.UPDATE+EndpointBundle.ID)
    public ResponseEntity<ResponseWrapper<PriorityResponseDto>> updatePriority(
            @PathVariable Long id,
            @RequestBody PriorityRequestDto requestDto) {
        PriorityResponseDto response = priorityService.updatePriority(id, requestDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.UPDATE_SUCCESSFULL,
                        response
                ));
    }
    @GetMapping(EndpointBundle.VIEW)
    public ResponseEntity<ResponseWrapper<Page<PriorityResponseDto>>> getAllPriorities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {

        Page<PriorityResponseDto> response =
                priorityService.getAllPriorities(page, size);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        response
                )
        );
    }
    @PostMapping(EndpointBundle.CREATE)
    public ResponseEntity<ResponseWrapper<PriorityResponseDto>> createPriority(
            @RequestBody PriorityRequestDto requestDto) {
        PriorityResponseDto response = priorityService.createPriority(requestDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseWrapper<>(
                        RestApiResponseStatusCodes.CREATED.getCode(),
                        ValidationMessages.CREATED_SUCCESSFULL,
                        response
                ));
    }

    @DeleteMapping(EndpointBundle.DELETE+EndpointBundle.ID)
    public ResponseEntity<ResponseWrapper<String>> deletePriority(@PathVariable Long id) {



        priorityService.deletePriority(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseWrapper<>(
                        RestApiResponseStatusCodes.DELETED.getCode(),
                        ValidationMessages.DELETE_SUCCESS,
                        "Delete Succesful"

                ));
    }

}
