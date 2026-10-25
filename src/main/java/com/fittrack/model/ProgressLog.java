package com.fittrack.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fittrack.exception.InvalidInputException;

@Document(collection = "logs")
@CompoundIndex(name = "client_date_idx", def = "{'clientId': 1, 'date': 1}", unique = true)
public class ProgressLog {

    @Id
    private String id;
    private String clientId;
    private String planId;
    private String date;
    private List<String> completedExercises = new ArrayList<>();
    private List<String> completedMeals = new ArrayList<>();
    private double completionPercent;
    private double weight;
    private String notes = "";

    public ProgressLog() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new InvalidInputException("Client is required");
        }
        this.clientId = clientId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        if (planId == null || planId.isBlank()) {
            throw new InvalidInputException("Plan is required");
        }
        this.planId = planId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        try {
            this.date = LocalDate.parse(date).toString();
        } catch (DateTimeParseException | NullPointerException e) {
            throw new InvalidInputException("Date must look like 2026-01-31");
        }
    }

    public List<String> getCompletedExercises() {
        return completedExercises;
    }

    public void setCompletedExercises(List<String> completedExercises) {
        this.completedExercises = completedExercises == null ? new ArrayList<>() : new ArrayList<>(completedExercises);
    }

    public List<String> getCompletedMeals() {
        return completedMeals;
    }

    public void setCompletedMeals(List<String> completedMeals) {
        this.completedMeals = completedMeals == null ? new ArrayList<>() : new ArrayList<>(completedMeals);
    }

    public double getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(double completionPercent) {
        if (completionPercent < 0 || completionPercent > 100) {
            throw new InvalidInputException("Completion must be between 0 and 100");
        }
        this.completionPercent = completionPercent;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        if (weight < 20 || weight > 500) {
            throw new InvalidInputException("Weight must be between 20 and 500 kg");
        }
        this.weight = weight;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        String cleaned = notes == null ? "" : notes.trim();
        if (cleaned.length() > 500) {
            throw new InvalidInputException("Notes can be at most 500 characters");
        }
        this.notes = cleaned;
    }
}
