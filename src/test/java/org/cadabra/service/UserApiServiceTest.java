package org.cadabra.service;

import org.cadabra.dto.UserApiResponse;
import org.cadabra.exception.UserApiFetchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserApiService userApiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userApiService, "apiUrl", "https://api.test/users");
    }

    @Test
    void shouldReturnUsersWhenApiResponds() {
        var user1 = new UserApiResponse();
        user1.setId(1L);
        user1.setName("John");
        var user2 = new UserApiResponse();
        user2.setId(2L);
        user2.setName("Jane");

        when(restTemplate.getForObject("https://api.test/users", UserApiResponse[].class))
                .thenReturn(new UserApiResponse[]{user1, user2});

        var result = userApiService.fetchAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John");
        assertThat(result.get(1).getName()).isEqualTo("Jane");
    }

    @Test
    void shouldReturnEmptyListWhenApiReturnsNull() {
        when(restTemplate.getForObject("https://api.test/users", UserApiResponse[].class))
                .thenReturn(null);

        var result = userApiService.fetchAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowUserApiFetchExceptionWhenApiCallFails() {
        when(restTemplate.getForObject("https://api.test/users", UserApiResponse[].class))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> userApiService.fetchAllUsers())
                .isInstanceOf(UserApiFetchException.class)
                .hasMessageContaining("Failed to fetch users from API");
    }
}

