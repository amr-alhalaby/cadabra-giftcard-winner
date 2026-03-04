package org.example.steps.user;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.dto.UserApiResponse;
import org.example.mapper.UserMapper;
import org.example.model.User;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Converts UserApiResponse DTO to User entity using MapStruct.
 */
@Component
@RequiredArgsConstructor
public class UserApiProcessor implements ItemProcessor<UserApiResponse, User> {

    private final UserMapper userMapper;

    @Override
    public User process(@NonNull UserApiResponse dto) {
        return userMapper.toUser(dto);
    }
}

