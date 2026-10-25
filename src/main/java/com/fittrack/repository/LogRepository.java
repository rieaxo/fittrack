package com.fittrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fittrack.model.ProgressLog;

public interface LogRepository extends MongoRepository<ProgressLog, String> {

    List<ProgressLog> findByClientIdOrderByDateAsc(String clientId);

    Optional<ProgressLog> findByClientIdAndDate(String clientId, String date);
}
