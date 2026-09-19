package com.sgic.defect_tracker.exceptionHandlers;

public class NoSuchElementException extends RuntimeException{
    public NoSuchElementException(String message){
        super(message);
    }
}
