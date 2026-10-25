package com.fittrack.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fittrack.exception.InvalidInputException;

@Document(collection = "plans")
public class Plan {

    @Id
    private String id;
    private String name;
    private int durationWeeks;
    private String trainerId;
    private String clientId;
    private List<Exercise> exercises = new ArrayList<>();
    private List<Meal> meals = new ArrayList<>();
    private long createdAt;

    public Plan() {
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
        if (name == null || name.trim().length() < 2 || name.trim().length() > 60) {
            throw new InvalidInputException("Plan name must be 2 to 60 characters");
        }
        this.name = name.trim();
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(int durationWeeks) {
        if (durationWeeks < 1 || durationWeeks > 52) {
            throw new InvalidInputException("Duration must be between 1 and 52 weeks");
        }
        this.durationWeeks = durationWeeks;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        if (trainerId == null || trainerId.isBlank()) {
            throw new InvalidInputException("Trainer is required");
        }
        this.trainerId = trainerId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new InvalidInputException("Please choose a client for this plan");
        }
        this.clientId = clientId;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        List<Exercise> checked = exercises == null ? new ArrayList<>() : new ArrayList<>(exercises);
        checked.forEach(Exercise::requireComplete);
        this.exercises = checked;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        List<Meal> checked = meals == null ? new ArrayList<>() : new ArrayList<>(meals);
        checked.forEach(Meal::requireComplete);
        this.meals = checked;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
