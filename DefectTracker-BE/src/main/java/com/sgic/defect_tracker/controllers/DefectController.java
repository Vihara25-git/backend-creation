package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.DefectFilterDTO;
import com.sgic.defect_tracker.dtos.request.DefectRequestDTO;
import com.sgic.defect_tracker.dtos.response.DefectByModuleResponseDTO;
import com.sgic.defect_tracker.dtos.response.DefectResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.DefectService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sgic.defect_tracker.dtos.response.ImportDefectResponseDTO;

import static com.sgic.defect_tracker.utils.EndpointBundle.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(DEFECT)
public class DefectController  {

    @Autowired
    private DefectService defectService;
//    @PostMapping(value=CREATE,
//                    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public ResponseWrapper<DefectResponseDTO> createdefect(@Valid @mul DefectRequestDTO defectRequestDTO
//    ){
//        return new ResponseWrapper<>(RestApiResponseStatusCodes.SUCCESS.getCode(),RestApiResponseStatusCodes.SUCCESS.getMessage(), defectService.createdefect(defectRequestDTO));
//
//
//    }

    @PostMapping( value = CREATE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseWrapper<DefectResponseDTO> createdefect( @Valid @RequestPart("defect") DefectRequestDTO defectRequestDTO,
                                                            @RequestPart( value = "attachmentImage", required = false ) MultipartFile attachmentImage)
    { return new ResponseWrapper<>( RestApiResponseStatusCodes.SUCCESS.getCode(),
            RestApiResponseStatusCodes.SUCCESS.getMessage(),
            defectService.createdefect( defectRequestDTO, attachmentImage ) ); }

    @PatchMapping(value = PUTDEFECT,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseWrapper<DefectResponseDTO> updatedefect( @Valid @RequestPart("defect") DefectRequestDTO defectRequestDTO,
                                                           @PathVariable Long id,
                                                            @RequestPart(value = "attachmentImage", required = false) MultipartFile attachmentImage
                                                            ){
        return  new ResponseWrapper<>(RestApiResponseStatusCodes.SUCCESS.getCode(),RestApiResponseStatusCodes.SUCCESS.getMessage(), defectService.updatedefect( id,defectRequestDTO,attachmentImage));
//
    }


    @GetMapping(GETDEFECT)
    public ResponseEntity<ResponseWrapper<Object>> getDefects(Pageable pageable){
        Page<DefectResponseDTO> responseDTO = defectService.GetDefectAll(pageable);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }

    @GetMapping(GETBYPROJECTIDDEFECT)
    public ResponseEntity<ResponseWrapper<Object>> getDefectByProjectId(@PathVariable("projectId") Long projectId,
                                                                        Pageable pageable){
        Page<DefectResponseDTO> responseDTOS = defectService.getProjectId_Defect(projectId,pageable);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTOS
                )
        );

    }

    @GetMapping(GETBY_DEFECTID_DEFECT)
    public ResponseEntity<ResponseWrapper<Object>> getDefectByDefectid(@PathVariable("defectId") Long defectId){

        DefectResponseDTO responseDTOS = defectService.getDefectById(defectId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTOS
                )
        );
    }


    @GetMapping(GETBY_STATUSID_DEFECT)
    public ResponseEntity<ResponseWrapper<Object>> getDefectByStatusId(
            @PathVariable("statusTypeId") Long statusTypeId){

        List<DefectResponseDTO> responseDTOS =
                defectService.getDefectByStatusTypeId(statusTypeId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTOS
                )
        );
    }

    @GetMapping(GETBY_SEVERITYID_DEFECT)
    public ResponseEntity<ResponseWrapper<Object>> getDefectBySeverityId(
            @PathVariable("severityId") Long severityId){

        List<DefectResponseDTO> responseDTOS =
                defectService.getDefectByseverityId(severityId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTOS
                )
        );
    }

    @GetMapping(GETBY_PRIORITYID_DEFECT)
    public ResponseEntity<ResponseWrapper<Object>> getDefectByPriorityId(
            @PathVariable("priorityId") Long priorityId){

        List<DefectResponseDTO> responseDTOS =
                defectService.getDefectBypriorityId(priorityId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTOS
                )
        );
    }
    @GetMapping(GETBY_DEFECTTYPEID_DEFECT)
    public ResponseEntity<ResponseWrapper<Object>> getDefectByDefecttypeId(
            @PathVariable("defectTypeId") Long defectTypeId){

        List<DefectResponseDTO> responseDTOS =
                defectService.getDefectBydefectTypeId(defectTypeId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTOS
                )
        );
    }
    @GetMapping("/filter")
    public ResponseEntity<ResponseWrapper<Object>> filterDefects(
            @RequestParam(required = false)Long projectId,
            @RequestParam(required = false) Long statusTypeId,
            @RequestParam(required = false) Long severityId,
            @RequestParam(required = false) Long priorityId,
            @RequestParam(required = false) Long defectTypeId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long subModuleId,
            @RequestParam(required = false) String search



    ){
        System.out.println("search = " + search);
        System.out.println("projectId = " + projectId);
        DefectFilterDTO dto=new DefectFilterDTO();
        dto.setDefectTypeId(defectTypeId);
        dto.setProjectId(projectId);
        dto.setStatusTypeId(statusTypeId);
        dto.setSeverityId(severityId);
        dto.setPriorityId(priorityId);
//        dto.setDefectTypeId(defectTypeId);
        dto.setModuleId(moduleId);
        dto.setSubModuleId(subModuleId);
        dto.setSearch(search);


        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        200,
                        "Success",
                        defectService.filterDefects(dto)
                )
        );
    }

    @PostMapping("/filter")
    public ResponseEntity<ResponseWrapper<Object>> filterDefectsPost(
            @RequestBody(required = false) DefectFilterDTO requestDto,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long statusTypeId,
            @RequestParam(required = false) Long severityId,
            @RequestParam(required = false) Long priorityId,
            @RequestParam(required = false) Long defectTypeId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long subModuleId,
            @RequestParam(required = false) String search
    ){
        DefectFilterDTO dto = requestDto != null ? requestDto : new DefectFilterDTO();
        if (dto.getProjectId() == null && projectId != null) dto.setProjectId(projectId);
        if (dto.getStatusTypeId() == null && statusTypeId != null) dto.setStatusTypeId(statusTypeId);
        if (dto.getSeverityId() == null && severityId != null) dto.setSeverityId(severityId);
        if (dto.getPriorityId() == null && priorityId != null) dto.setPriorityId(priorityId);
        if (dto.getDefectTypeId() == null && defectTypeId != null) dto.setDefectTypeId(defectTypeId);
        if (dto.getModuleId() == null && moduleId != null) dto.setModuleId(moduleId);
        if (dto.getSubModuleId() == null && subModuleId != null) dto.setSubModuleId(subModuleId);
        if (dto.getSearch() == null && search != null) dto.setSearch(search);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        200,
                        "Success",
                        defectService.filterDefects(dto)
                )
        );
    }
    @PatchMapping("/bulk-reassign")
    public ResponseEntity<ResponseWrapper<Object>> bulkReassignDefects(
            @RequestBody Map<String, Object> request) {

        List<Integer> defectIds =
                (List<Integer>) request.get("defectIds");

        Number assignedToId =
                (Number) request.get("assignedToId");

        if (defectIds == null || assignedToId == null) {
            return ResponseEntity.badRequest().body(
                    new ResponseWrapper<>(
                            400,
                            "defectIds and assignedToId are required",
                            null
                    )
            );
        }

        List<Long> ids = defectIds.stream()
                .map(Long::valueOf)
                .toList();

        defectService.bulkReassignDefects(
                ids,
                assignedToId.longValue()
        );

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Defects reassigned successfully",
                        null
                )
        );
    }
    @DeleteMapping(DELETEDEFECT)
    public ResponseEntity<ResponseWrapper<Object>> delete(@PathVariable("defectId") Long id){
        defectService.deleteDefect(id);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.DELETE_SUCCESS,
                        null
                )
        );
    }
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportDefects(
            @RequestParam Long projectId) {

        byte[] file =
                defectService.exportDefectsToExcel(
                        projectId
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=defects.xlsx"
                )
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .body(file);
    }

// for Dashboard - defects by module
@GetMapping("/module-summary/{projectId}")
public ResponseEntity<Map<String, Object>> getDefectsByModule(@PathVariable Long projectId) {
    List<DefectByModuleResponseDTO> list = defectService.getDefectsByModule(projectId);

    Map<String, Object> response = new HashMap<>();
    response.put("status", "SUCCESS");
    response.put("statusCode", 200);
    response.put("statusMessage", "Retrieved");
    response.put("data", list);

    return ResponseEntity.ok(response);
}




    //import defects
    @PostMapping("/bulk")
    public ResponseEntity<?> importDefects(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long projectId
    ) {
        ImportDefectResponseDTO response =
                defectService.importDefects(file, projectId);

        return ResponseEntity.ok(response);
    }

}
