package com.sgic.defect_tracker.controllers;


//import com.sgic.defect_tracker.dtos.DefectDto;

import com.sgic.defect_tracker.dtos.request.DefectTypeRequestDto;
import com.sgic.defect_tracker.dtos.response.DefectTypeResponseDto;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.DefectTypeService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sgic.defect_tracker.utils.EndpointBundle.*;


@Service
@RestController
@RequestMapping(defect)
public class DefectTypeController {
    @Autowired
    private DefectTypeService defectService;

    @PostMapping(create)
    public ResponseWrapper<DefectTypeResponseDto> defectsave(@Valid @RequestBody DefectTypeRequestDto defect) {

        return new ResponseWrapper<>(RestApiResponseStatusCodes.SUCCESS.getCode(), RestApiResponseStatusCodes.SUCCESS.getMessage(), defectService.defectsave(defect));


    };

    @GetMapping(get)
    public ResponseWrapper<Page<DefectTypeResponseDto>> defectgetall(Pageable pageable){
        return new ResponseWrapper<>(RestApiResponseStatusCodes.SUCCESS.getCode(), RestApiResponseStatusCodes.SUCCESS.getMessage(), defectService.defectgetall(pageable));


    };

    @DeleteMapping(delete)
    public ResponseWrapper<String> defectdelete(@PathVariable Long id){
        return new ResponseWrapper<>(RestApiResponseStatusCodes.SUCCESS.getCode(), RestApiResponseStatusCodes.SUCCESS.getMessage(), defectService.defectdelete(id));

    }

    @PatchMapping(update)
    public ResponseWrapper<DefectTypeResponseDto> defectupdate(@PathVariable Long defectTypeId,@Valid
                                                               @RequestBody DefectTypeRequestDto defect
    ){
        return new ResponseWrapper<>(RestApiResponseStatusCodes.SUCCESS.getCode(), RestApiResponseStatusCodes.SUCCESS.getMessage(), defectService.updatedefect(defect,defectTypeId));

    }




}
