package org.example.repository;

import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = """
            SELECT u.* FROM purchases p
            INNER JOIN users u ON u.id = p.user_id
            WHERE p.job_execution_id = :jobExecutionId
            GROUP BY u.id
            HAVING SUM(p.amount) > :minAmount
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<User> findRandomEligibleUser(Long jobExecutionId, double minAmount);
}
