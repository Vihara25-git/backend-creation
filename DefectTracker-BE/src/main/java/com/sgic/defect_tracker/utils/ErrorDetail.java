package com.sgic.defect_tracker.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ErrorDetail {

    private Date timestamp;
    private String message;
    private String errorCode;

    public ErrorDetail(Date date, String message, String errorCode) {
        this.timestamp = new Date();
        this.message = message;
        this.errorCode = errorCode;
    }


}
