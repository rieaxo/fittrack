package com.fittrack.model;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fittrack.exception.InvalidInputException;

@Document(collection = "users")
public class Client extends User {

    private String trainerId;

    public Client() {
    }

    public Client(String name, String email, String passwordHash, String trainerId) {
        super(name, email, passwordHash);
        setTrainerId(trainerId);
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        if (trainerId == null || trainerId.isBlank()) {
            throw new InvalidInputException("Please choose a trainer");
        }
        this.trainerId = trainerId;
    }

    @Override
    @Transient
    public String getDashboardPath() {
        return "client.html";
    }

    @Override
    @Transient
    public String getRole() {
        return "CLIENT";
    }
}
