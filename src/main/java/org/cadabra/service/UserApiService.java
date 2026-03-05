package org.cadabra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cadabra.dto.UserApiResponse;
import org.cadabra.exception.UserApiFetchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Responsible for fetching users from the external API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApiService {

    @Value("${integration.user-api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public List<UserApiResponse> fetchAllUsers() {
        log.info("Fetching users from API: {}", apiUrl);
        try {
            UserApiResponse[] users = restTemplate.getForObject(apiUrl, UserApiResponse[].class);
            if (users != null) {
                log.info("Fetched {} users from API", users.length);
                return List.of(users);
            }
            log.warn("No users returned from API");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch users from API: {}", e.getMessage(), e);
            throw new UserApiFetchException("Failed to fetch users from API", e);
        }
    }
}

