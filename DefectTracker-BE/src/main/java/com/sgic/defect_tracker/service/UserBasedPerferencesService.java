
package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.UserBasedPerferencesRequestDTO;
import com.sgic.defect_tracker.dtos.request.UserNotificationUpdateRequestDTO;
import com.sgic.defect_tracker.dtos.response.UserBasedPerferencesResponseDTO;

import java.util.List;
import java.util.Map;

public interface UserBasedPerferencesService {

    UserBasedPerferencesResponseDTO create(
            UserBasedPerferencesRequestDTO requestDTO
    );

    UserBasedPerferencesResponseDTO getById(Long id);

    List<UserBasedPerferencesResponseDTO> getAll();

    UserBasedPerferencesResponseDTO update(
            Long id,
            UserBasedPerferencesRequestDTO requestDTO
    );

    Map<Long, String> getUserChannels(Long userId);

    void updateUserChannels(UserNotificationUpdateRequestDTO request);

    void delete(Long id);
}

//package com.sgic.defect_tracker.service;
//
//import com.sgic.defect_tracker.dtos.request.UserBasedPerferencesRequestDTO;
//import com.sgic.defect_tracker.dtos.response.UserBasedPerferencesResponseDTO;
//
//import java.util.List;
//
//public interface UserBasedPerferencesService {
//
//    UserBasedPerferencesResponseDTO create(
//            UserBasedPerferencesRequestDTO request
//    );
//
//    List<UserBasedPerferencesResponseDTO> getAll();
//
//    UserBasedPerferencesResponseDTO getById(Long id);
//
//    List<UserBasedPerferencesResponseDTO> getByEmployee(Long empId);
//
//    UserBasedPerferencesResponseDTO update(
//            Long id,
//            UserBasedPerferencesRequestDTO request
//    );
//
//    void delete(Long id);
//}