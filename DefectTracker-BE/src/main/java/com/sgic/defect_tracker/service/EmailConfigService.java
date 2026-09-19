package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.EmailConfigRequestDTO;
import com.sgic.defect_tracker.dtos.response.EmailConfigResponseDTO;
import com.sgic.defect_tracker.entities.EmailConfig;

import java.util.List;

public interface EmailConfigService {

    EmailConfig createEmailConfig(EmailConfigRequestDTO request);

    EmailConfigResponseDTO toggleStatus(Long id);

    List<EmailConfigResponseDTO> getAllEmailConfigs();

    EmailConfigResponseDTO getEmailConfig(Long id);

    EmailConfigResponseDTO updateEmailConfig(
            Long id,
            EmailConfigRequestDTO requestDTO
    );



    void deleteEmailConfig(Long id);


}

