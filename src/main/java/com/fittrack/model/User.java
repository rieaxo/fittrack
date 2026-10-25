package com.fittrack.model;

import java.util.regex.Pattern;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fittrack.exception.InvalidInputException;

@Document(collection = "users")
public abstract class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String email;
    private String passwordHash;

    protected User() {
    }

    protected User(String name, String email, String passwordHash) {
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().length() < 2) {
            throw new InvalidInputException("Name must be at least 2 characters");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidInputException("Enter a valid email address");
        }
        this.email = email.trim().toLowerCase();
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidInputException("Password is required");
        }
        this.passwordHash = passwordHash;
    }

    @Transient
    public abstract String getDashboardPath();

    @Transient
    public abstract String getRole();
}
