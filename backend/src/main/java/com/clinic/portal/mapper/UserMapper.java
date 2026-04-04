package com.clinic.portal.mapper;

import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toDto(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isActive()
        );
    }
}
