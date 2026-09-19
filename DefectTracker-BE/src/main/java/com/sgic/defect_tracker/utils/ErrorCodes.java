package com.sgic.defect_tracker.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource("classpath:ErrorMessages.properties")
public class ErrorCodes {

    @Value("${validation.badRequest}")
    private String badRequest;

    @Value("${validation.alreadyExist}")
    private String alreadyExist;

    @Value("${validation.notFound}")
    private String notFound;

    @Value("${validation.notValid}")
    private String notValid;

    @Value("${validation.missingField}")
    private String missingField;

    @Value("${validation.typeMismatch}")
    private String typeMismatch;

    @Value("${validation.methodNotAllowed}")
    private String methodNotAllowed;

    @Value("${validation.invalidFormat}")
    private String invalidFormat;

//    public Object getNotFound() {
//
//        return null;
//    }
}
