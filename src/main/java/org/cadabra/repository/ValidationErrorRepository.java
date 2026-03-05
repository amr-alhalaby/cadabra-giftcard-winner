package org.cadabra.repository;

import org.cadabra.model.ValidationError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ValidationErrorRepository extends JpaRepository<ValidationError, UUID> {

    List<ValidationError> findByJobExecutionId(Long jobExecutionId);
}
