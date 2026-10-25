package com.fittrack.model;

import com.fittrack.exception.InvalidInputException;

public class Meal {

    private String name;
    private int calories;

    public Meal() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("Meal name is required");
        }
        this.name = name.trim();
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        if (calories < 1 || calories > 5000) {
            throw new InvalidInputException("Calories must be between 1 and 5000");
        }
        this.calories = calories;
    }

    public void requireComplete() {
        if (name == null || calories == 0) {
            throw new InvalidInputException("Each meal needs a name and calories");
        }
    }
}
