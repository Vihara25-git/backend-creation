package com.sgic.defect_tracker.enums;

public enum RestApiResponseStatusCodes {
    SUCCESS(200, "Success"),
    NO_CHANGES(200,"No changes"),
    CREATED(201, "Created"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    CONFLICT(409, "Conflict"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    UPDATED(200, "Updated"),
    DELETED(204, "Deleted"),
    VALIDATION_FAILED(422, "Validation Failed");

    private final int code;
    private final String message;

    RestApiResponseStatusCodes(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
