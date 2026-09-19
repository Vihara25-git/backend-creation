package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data
public class EmailConfigResponseDTO {

    private Long id;
    private String name;
    private String fromEmail;
    private String host;
    private Integer port;
    private Boolean status;
    private String smtpUserName;
//    private String smtpPassword;
    private String fromName;
}
