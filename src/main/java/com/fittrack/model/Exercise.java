package com.fittrack.model;

import com.fittrack.exception.InvalidInputException;

public class Exercise {

    private String name;
    private int sets;
    private int reps;

    public Exercise() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("Exercise name is required");
        }
        this.name = name.trim();
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        if (sets < 1 || sets > 100) {
            throw new InvalidInputException("Sets must be between 1 and 100");
        }
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        if (reps < 1 || reps > 1000) {
            throw new InvalidInputException("Reps must be between 1 and 1000");
        }
        this.reps = reps;
    }

    public void requireComplete() {
        if (name == null || sets == 0 || reps == 0) {
            throw new InvalidInputException("Each exercise needs a name, sets and reps");
        }
    }
}
