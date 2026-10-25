package com.fittrack.dto;

import java.util.List;

public record LogRequest(List<String> completedExercises, List<String> completedMeals, Double weight, String notes) {
}
