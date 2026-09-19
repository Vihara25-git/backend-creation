package com.sgic.defect_tracker.dtos.request;
import jakarta.validation.constraints.Pattern;


import lombok.Data;

@Data
public class ClientDetailsRequestDto {

    //  private Long clientId;
    private String clientName;
    private String country;
    private String state;


    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private  String phoneNumber;

    @Pattern(
            regexp = "^(?!.*\\.\\.)[a-zA-Z0-9](?:[a-zA-Z0-9.]*)[a-zA-Z0-9]@gmail\\.com$",
            message = "Please enter a valid Gmail address"
    )
    private String email;
}
