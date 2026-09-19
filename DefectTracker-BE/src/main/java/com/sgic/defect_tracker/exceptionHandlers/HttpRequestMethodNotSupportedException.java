package com.sgic.defect_tracker.exceptionHandlers;

public class HttpRequestMethodNotSupportedException extends RuntimeException{
    public HttpRequestMethodNotSupportedException(String message){
        super(message);
    }
}
