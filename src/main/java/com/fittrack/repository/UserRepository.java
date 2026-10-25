package com.fittrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.fittrack.model.User;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("{ 'trainerId': ?0 }")
    List<User> findClientsByTrainerId(String trainerId);

    @Query("{ '_class': 'com.fittrack.model.Trainer' }")
    List<User> findAllTrainers();
}
