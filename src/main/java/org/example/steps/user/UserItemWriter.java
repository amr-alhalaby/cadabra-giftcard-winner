package org.example.steps.user;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserItemWriter implements ItemWriter<User> {

    private final UserRepository userRepository;

    @Override
    public void write(Chunk<? extends User> chunk) {
        userRepository.saveAll(chunk.getItems());
    }
}

