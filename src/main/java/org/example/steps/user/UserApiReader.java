package org.example.steps.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.UserApiResponse;
import org.example.service.UserApiService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;

/**
 * Reads users one by one for Spring Batch processing.
 * Delegates the actual API call to UserApiService.
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class UserApiReader implements ItemReader<UserApiResponse> {

    private final UserApiService userApiService;
    private Iterator<UserApiResponse> userIterator;

    @Override
    public UserApiResponse read() {
        if (userIterator == null) {
            log.debug("Fetching users from API...");
            userIterator = userApiService.fetchAllUsers().iterator();
            log.debug("User API returned iterator — starting to read users");
        }

        if (userIterator.hasNext()) {
            UserApiResponse user = userIterator.next();
            log.debug("Read user — id: {}, name: {}", user.getId(), user.getName());
            return user;
        }
        log.debug("No more users to read — returning null to signal end of data");
        return null;
    }
}
