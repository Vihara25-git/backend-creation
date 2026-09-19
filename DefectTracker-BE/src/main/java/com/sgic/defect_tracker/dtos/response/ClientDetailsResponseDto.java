package com.sgic.defect_tracker.dtos.response;


import lombok.Data;

@Data
public class ClientDetailsResponseDto {

    private Long clientId;
    private String clientName;
    private String country;
    private  String state;
    private String email;
    private String phoneNumber;


}
