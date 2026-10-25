package com.fittrack.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fittrack.exception.InvalidInputException;
import com.fittrack.model.Client;
import com.fittrack.model.Plan;
import com.fittrack.repository.PlanRepository;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final AuthService authService;

    public PlanService(PlanRepository planRepository, AuthService authService) {
        this.planRepository = planRepository;
        this.authService = authService;
    }

    public Plan create(Plan plan) {
        validate(plan);
        plan.setId(null);
        plan.setCreatedAt(System.currentTimeMillis());
        return planRepository.save(plan);
    }

    public List<Plan> listByTrainer(String trainerId) {
        authService.getTrainer(trainerId);
        return planRepository.findByTrainerIdOrderByCreatedAtDesc(trainerId);
    }

    public Plan get(String id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new InvalidInputException("Plan not found"));
    }

    public Plan update(String id, Plan incoming) {
        Plan existing = get(id);
        validate(incoming);
        if (!existing.getTrainerId().equals(incoming.getTrainerId())) {
            throw new InvalidInputException("You can only edit your own plans");
        }
        existing.setName(incoming.getName());
        existing.setDurationWeeks(incoming.getDurationWeeks());
        existing.setClientId(incoming.getClientId());
        existing.setExercises(incoming.getExercises());
        existing.setMeals(incoming.getMeals());
        return planRepository.save(existing);
    }

    public void delete(String id) {
        get(id);
        planRepository.deleteById(id);
    }

    public Optional<Plan> getPlanForClient(String clientId) {
        authService.getClient(clientId);
        return planRepository.findFirstByClientIdOrderByCreatedAtDesc(clientId);
    }

    private void validate(Plan plan) {
        if (plan == null) {
            throw new InvalidInputException("Plan details are required");
        }
        if (plan.getTrainerId() == null) {
            throw new InvalidInputException("Trainer is required");
        }
        authService.getTrainer(plan.getTrainerId());
        if (plan.getName() == null) {
            throw new InvalidInputException("Plan name is required");
        }
        if (plan.getDurationWeeks() == 0) {
            throw new InvalidInputException("Duration is required");
        }
        if (plan.getClientId() == null) {
            throw new InvalidInputException("Please choose a client for this plan");
        }
        Client client = authService.getClient(plan.getClientId());
        if (!plan.getTrainerId().equals(client.getTrainerId())) {
            throw new InvalidInputException("This client does not belong to you");
        }
        if (plan.getExercises().isEmpty() && plan.getMeals().isEmpty()) {
            throw new InvalidInputException("Add at least one exercise or meal");
        }
    }
}
