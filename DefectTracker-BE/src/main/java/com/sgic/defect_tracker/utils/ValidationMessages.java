package com.sgic.defect_tracker.utils;

public class ValidationMessages {
    public static final String CREATED_SUCCESSFULL = "User created successfully.";
    public static final String CREATED_FAILED = "Failed to create user.";
    public static final String SAVED_SUCCESSFULL = "Saved successfully.";
    public static final String SAVE_FAILED = "Save Failed.";
    public static final String DELETE_SUCCESS = "Delete successfully";
    public static final String DELETE_FAILED = "Delete Failed";
    public static final String RETRIEVED = "Retrieved Successfully.";
    public static final String RETRIEVED_FAILED = "Retrieve Failed.";
    public static final String UPDATE_SUCCESSFULL = "Update successfully";
    public static final String UPDATE_FAILED = "update Failed";
    public static final String BAD_REQUEST="bad request";
    public static final String INVALID_INPUT="invalid input";
    public static final String  VALIDATION_FAILED="validation failed";
    public static final String  NOT_FOUND="not found";

    // Exception Handler
    public static final String FOREIGN_KEY_CONSTRAINT = "Unable to Delete: This Record Linked to Another Record.";
    public static final String DUPLICATE_ENTRY = "Duplicate Entry: You are try to insert a data that already in the database.";
    public static final String WRONG_API_CALL = "Wrong API method or path: Please ensure that your API path and method are correct.";
    public static final String WRONG_JSON = "JSON is not in a valid format.";
    public static final String MIN_REQUIREMENT = "At least one attribute must be present";

    // Login
    public static final String EMAIL_NOT_EMPTY = "please enter email address";
    public static final String INVALID_NAME = "Firstname can contain only A-Z or a-z characters only.";
    public static final String INVALID_CREDENTIAL = "Invalid email or password";
    public static final String EMAIL_VERIFIED = "Email verified successfully.";
    public static final String EMAIL_NOT_VERIFIED = "Invalid or expired verification code.";
    public static final String LOGIN_SUCCESSFULL = "Login successful";

    // Invalid Fields
    public static final String INVALID_ID = "Invalid ID: No Value Present.";

    //Others
    public static final String MISMATCH_INPUT = "Input is not in a valid format.";

    // Name validations
    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String LAST_NAME_REQUIRED = "Last name is required";
    public static final String NAME_LENGTH = "Name must be between 2 and 20 characters";
    public static final String NAME_PATTERN = "Name must contain only alphabets and single spaces";
    public static final String REQUIRED = "Must Required this field";

    // Email validations
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String INVALID_EMAIL = "An email should be in the form of: your-email@example.com";


    public static final String PROJECTID_NOT = "Project is not availabla";
}