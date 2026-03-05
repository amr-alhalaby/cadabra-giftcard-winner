package org.cadabra.steps.user;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cadabra.dto.UserApiResponse;
import org.cadabra.mapper.UserMapper;
import org.cadabra.model.User;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Converts UserApiResponse DTO to User entity using MapStruct.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserApiProcessor implements ItemProcessor<UserApiResponse, User> {

    private final UserMapper userMapper;

    @Override
    public User process(@NonNull UserApiResponse dto) {
        log.debug("Processing user API response — id: {}, name: {}", dto.getId(), dto.getName());
        User user = userMapper.toUser(dto);
        log.debug("Mapped user entity — id: {}, name: {}", user.getId(), user.getName());
        return user;
    }
}
