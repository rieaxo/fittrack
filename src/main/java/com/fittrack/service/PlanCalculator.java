package com.fittrack.service;

import java.util.List;

import com.fittrack.model.ProgressLog;

public interface PlanCalculator {

    double calculateAdherence(List<ProgressLog> logs);
}
