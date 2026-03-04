package org.example.steps.user;

import lombok.RequiredArgsConstructor;
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
@Component
@StepScope
@RequiredArgsConstructor
public class UserApiReader implements ItemReader<UserApiResponse> {

    private final UserApiService userApiService;
    private Iterator<UserApiResponse> userIterator;

    @Override
    public UserApiResponse read() {
        if (userIterator == null) {
            userIterator = userApiService.fetchAllUsers().iterator();
        }

        if (userIterator.hasNext()) {
            return userIterator.next();
        }
        return null;
    }
}
