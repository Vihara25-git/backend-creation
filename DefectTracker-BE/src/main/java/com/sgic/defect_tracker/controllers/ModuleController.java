package com.sgic.defect_tracker.controllers;


import com.sgic.defect_tracker.dtos.request.ModuleRequestDto;
import com.sgic.defect_tracker.dtos.response.ModuleResponseDto;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.repositories.ModuleRepositories;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import com.sgic.defect_tracker.service.ModuleService;

import java.util.List;

import static com.sgic.defect_tracker.utils.EndpointBundle.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(EndpointBundle.MODULE)
public class ModuleController {

   private final ModuleService moduleService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<ModuleResponseDto>> createModuleByProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ModuleRequestDto moduleRequestDto) {

        ModuleResponseDto createdModule = moduleService.createModule(projectId, moduleRequestDto);

        ResponseWrapper<ModuleResponseDto> response = new ResponseWrapper<>(
                RestApiResponseStatusCodes.CREATED.getCode(),
                ValidationMessages.SAVED_SUCCESSFULL,
                createdModule
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //PUT
    @PutMapping(EndpointBundle.UPDATEMODULE)
    public ResponseEntity<ResponseWrapper<String>> updateModule(@PathVariable Long moduleId,
                                                                @Valid @RequestBody ModuleRequestDto requestDto){
        moduleService.updateModule(moduleId, requestDto);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.UPDATED.getCode(),
                        RestApiResponseStatusCodes.UPDATED.getMessage(),
                        null
                )
        );
    }


    //delete
    @DeleteMapping(EndpointBundle.DELETEMODULE)
    public ResponseEntity<ResponseWrapper<String>>deleteModule(
            @PathVariable Long moduleId){

        moduleService.deleteModule(moduleId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.DELETED.getCode(),
                        RestApiResponseStatusCodes.DELETED.getMessage(),
                        "Success"
                )
        );
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ModuleResponseDto>>> getAllModule  (@PathVariable Long projectId,
    @RequestParam(required = false) String name)
   {
        List<ModuleResponseDto> modules = moduleService.getAllModule(projectId,name);
        if(modules.isEmpty()){
                    ResponseWrapper<List<ModuleResponseDto>> response = new ResponseWrapper<>(
                    RestApiResponseStatusCodes.NOT_FOUND.getCode(),
                    ValidationMessages.RETRIEVED_FAILED,
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS.getCode(),
                ValidationMessages.RETRIEVED,
                modules
        ));
    }

//    //VIEWMODULEBYID
//    @GetMapping(VIEWMODULEBYID)
//    public ResponseEntity<ResponseWrapper<ModuleResponseDto>> getModuleById(
//            @PathVariable("moduleId") Long moduleId) {
//
//        ModuleResponseDto response = moduleService.getModuleById(moduleId);
//
//        return ResponseEntity.ok(
//                new ResponseWrapper<>(
//                        RestApiResponseStatusCodes.SUCCESS.getCode(),
//                        ValidationMessages.RETRIEVED,
//                        response
//                )
//        );
//    }
}
