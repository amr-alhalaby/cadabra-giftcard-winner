package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WinnerSelectionService {

    private final UserRepository userRepository;

    @Value("${winner.min-purchase-amount}")
    private double minPurchaseAmount;

    public Optional<User> announceWinner(Long jobExecutionId) {
        Optional<User> winner = userRepository.findRandomEligibleUser(jobExecutionId, minPurchaseAmount);
        winner.ifPresentOrElse(
                this::logWinner,
                () -> log.warn("No eligible winner found for job execution: {}", jobExecutionId)
        );
        return winner;
    }

    protected void logWinner(User w) {
        log.info("============================================================");
        log.info("  🎉 GIFT CARD WINNER 🎉");
        log.info("============================================================");
        log.info("  Winner ID : {}", w.getId());
        log.info("  Name      : {}", w.getName());
        log.info("  Username  : {}", w.getUsername());
        log.info("  Email     : {}", w.getEmail());
        log.info("  Phone     : {}", w.getPhone());
        log.info("============================================================");
    }
}
