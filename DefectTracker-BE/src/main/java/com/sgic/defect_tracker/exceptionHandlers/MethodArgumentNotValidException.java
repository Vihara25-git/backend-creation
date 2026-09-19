package com.sgic.defect_tracker.exceptionHandlers;
//org.springframework.web.bind.MethodArgumentNotValidException;

public class MethodArgumentNotValidException extends RuntimeException{
    public MethodArgumentNotValidException(String message){
        super(message);

    }
}
