package com.fittrack.model;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class Trainer extends User {

    public Trainer() {
    }

    public Trainer(String name, String email, String passwordHash) {
        super(name, email, passwordHash);
    }

    @Override
    @Transient
    public String getDashboardPath() {
        return "trainer.html";
    }

    @Override
    @Transient
    public String getRole() {
        return "TRAINER";
    }
}
