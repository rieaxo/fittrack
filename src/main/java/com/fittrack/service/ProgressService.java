package com.fittrack.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.fittrack.dto.LogRequest;
import com.fittrack.exception.InvalidInputException;
import com.fittrack.model.Client;
import com.fittrack.model.Exercise;
import com.fittrack.model.Meal;
import com.fittrack.model.Plan;
import com.fittrack.model.ProgressLog;
import com.fittrack.repository.LogRepository;

@Service
public class ProgressService {

    private final LogRepository logRepository;
    private final PlanService planService;
    private final AuthService authService;
    private final PlanCalculator calculator;
    private final MongoTemplate mongoTemplate;

    public ProgressService(LogRepository logRepository, PlanService planService, AuthService authService,
                           PlanCalculator calculator, MongoTemplate mongoTemplate) {
        this.logRepository = logRepository;
        this.planService = planService;
        this.authService = authService;
        this.calculator = calculator;
        this.mongoTemplate = mongoTemplate;
    }

    public ProgressLog saveLog(String clientId, LogRequest request) {
        if (request == null || request.weight() == null) {
            throw new InvalidInputException("Weight is required");
        }
        Plan plan = planService.getPlanForClient(clientId)
                .orElseThrow(() -> new InvalidInputException("No plan has been assigned to you yet"));

        Set<String> chosenExercises = toSet(request.completedExercises());
        Set<String> chosenMeals = toSet(request.completedMeals());
        List<String> doneExercises = plan.getExercises().stream()
                .map(Exercise::getName).filter(chosenExercises::contains).toList();
        List<String> doneMeals = plan.getMeals().stream()
                .map(Meal::getName).filter(chosenMeals::contains).toList();

        int total = plan.getExercises().size() + plan.getMeals().size();
        double percent = total == 0 ? 0 : WeeklyAdherenceCalculator.round1((doneExercises.size() + doneMeals.size()) * 100.0 / total);

        String today = LocalDate.now().toString();
        ProgressLog log = logRepository.findByClientIdAndDate(clientId, today).orElseGet(ProgressLog::new);
        log.setClientId(clientId);
        log.setPlanId(plan.getId());
        log.setDate(today);
        log.setCompletedExercises(doneExercises);
        log.setCompletedMeals(doneMeals);
        log.setCompletionPercent(percent);
        log.setWeight(request.weight());
        log.setNotes(request.notes());
        return logRepository.save(log);
    }

    public List<ProgressLog> getLogs(String clientId) {
        authService.getClient(clientId);
        return logRepository.findByClientIdOrderByDateAsc(clientId);
    }

    public Map<String, Object> getAdherence(String clientId) {
        List<ProgressLog> logs = getLogs(clientId);
        String start = WeeklyAdherenceCalculator.windowStart();
        long daysLogged = logs.stream().filter(log -> log.getDate().compareTo(start) >= 0).count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("adherence", calculator.calculateAdherence(logs));
        result.put("daysLogged", daysLogged);
        result.put("windowDays", WeeklyAdherenceCalculator.WINDOW_DAYS);
        return result;
    }

    public Map<String, Double> weeklyAdherenceByClient(String trainerId) {
        List<String> clientIds = authService.getClientsOf(trainerId).stream().map(Client::getId).toList();

        Map<String, Double> averages = new LinkedHashMap<>();
        clientIds.forEach(id -> averages.put(id, 0.0));
        if (clientIds.isEmpty()) {
            return averages;
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("clientId").in(clientIds)
                        .and("date").gte(WeeklyAdherenceCalculator.windowStart())),
                Aggregation.group("clientId").sum("completionPercent").as("total"));

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "logs", Document.class);
        for (Document row : results.getMappedResults()) {
            double total = ((Number) row.get("total")).doubleValue();
            averages.put(row.getString("_id"), WeeklyAdherenceCalculator.round1(total / WeeklyAdherenceCalculator.WINDOW_DAYS));
        }
        return averages;
    }

    private Set<String> toSet(List<String> values) {
        return values == null ? new HashSet<>() : new HashSet<>(values);
    }
}
