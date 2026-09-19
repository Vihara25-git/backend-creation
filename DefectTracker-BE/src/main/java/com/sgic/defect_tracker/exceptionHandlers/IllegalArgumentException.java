package com.sgic.defect_tracker.exceptionHandlers;

public class IllegalArgumentException extends RuntimeException {
    public IllegalArgumentException(String message) {
        super(message);
    }
}
