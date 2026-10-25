package com.fittrack.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fittrack.model.ProgressLog;

@Component
public class WeeklyAdherenceCalculator implements PlanCalculator {

    public static final int WINDOW_DAYS = 7;

    public static String windowStart() {
        return LocalDate.now().minusDays(WINDOW_DAYS - 1).toString();
    }

    public static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    @Override
    public double calculateAdherence(List<ProgressLog> logs) {
        String start = windowStart();
        double total = logs.stream()
                .filter(log -> log.getDate().compareTo(start) >= 0)
                .mapToDouble(ProgressLog::getCompletionPercent)
                .sum();
        return round1(total / WINDOW_DAYS);
    }
}
