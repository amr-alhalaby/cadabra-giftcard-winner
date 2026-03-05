package org.cadabra.steps.user;

import org.cadabra.dto.UserApiResponse;
import org.cadabra.mapper.UserMapper;
import org.cadabra.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApiProcessorTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserApiProcessor userApiProcessor;

    @Test
    void shouldMapDtoToUser() {
        UserApiResponse dto = new UserApiResponse();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setEmail("john@example.com");

        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(userMapper.toUser(dto)).thenReturn(user);

        User result = userApiProcessor.process(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }
}

