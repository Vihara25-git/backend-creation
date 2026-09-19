package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.StatusTypeRequestDTO;
import com.sgic.defect_tracker.dtos.response.StatusTypeResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.StatusTypeService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sgic.defect_tracker.utils.EndpointBundle.*;

@RestController
@RequestMapping(STATUS_TYPE)
@RequiredArgsConstructor
public class StatusTypeController {
    @Autowired
    private final StatusTypeService service;

    @PostMapping
    public ResponseEntity<ResponseWrapper<StatusTypeResponseDTO>> create(@Valid @RequestBody StatusTypeRequestDTO dto) {

        StatusTypeResponseDTO response = service.save(dto);

        ResponseWrapper<StatusTypeResponseDTO> wrapper =
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.SAVED_SUCCESSFULL,
                        response
                );

        return ResponseEntity.ok(wrapper);

    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<Object>> GetStatusType(Pageable pageable){
        Page<StatusTypeResponseDTO> responseDTO = service.GetStatusType(pageable);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }
    @DeleteMapping(STATUS_TYPEID)
    public ResponseEntity<ResponseWrapper<Object>>  Delete(@PathVariable Long id){

        service.delete(id);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.DELETE_SUCCESS,
                        null
                )
        );
    }

    @PutMapping(STATUS_TYPEID)
    public ResponseEntity<ResponseWrapper<Object>> UpdateStatusType(@PathVariable Long id,@Valid @RequestBody StatusTypeRequestDTO statusTypeRequestDTO) {

        StatusTypeResponseDTO responseDTO = service.UpdateStatusType(id, statusTypeRequestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.UPDATE_SUCCESSFULL,
                        responseDTO

                )
        );
    }


}