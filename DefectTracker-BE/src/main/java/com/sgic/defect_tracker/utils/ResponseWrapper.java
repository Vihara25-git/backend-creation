package com.sgic.defect_tracker.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sgic.defect_tracker.dtos.response.ReleaseTypeResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Data
public class ResponseWrapper<T>{
    private int statusCode;
    private String statusMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public ResponseWrapper(RestApiResponseStatusCodes restApiResponseStatusCodes, T data, String message) {
        this.statusCode = restApiResponseStatusCodes.getCode();
        this.statusMessage = message;
        this.data = data;
    }

//
//    public ResponseWrapper(int code, String message, T defectgetall) {
//    }
}
