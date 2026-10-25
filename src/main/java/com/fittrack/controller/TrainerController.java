package com.fittrack.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fittrack.model.Client;
import com.fittrack.model.Plan;
import com.fittrack.service.AuthService;
import com.fittrack.service.PlanService;
import com.fittrack.service.ProgressService;

@RestController
@RequestMapping("/api")
public class TrainerController {

    private final AuthService authService;
    private final PlanService planService;
    private final ProgressService progressService;

    public TrainerController(AuthService authService, PlanService planService, ProgressService progressService) {
        this.authService = authService;
        this.planService = planService;
        this.progressService = progressService;
    }

    @GetMapping("/trainer/{id}/clients")
    public List<Client> clients(@PathVariable("id") String id) {
        return authService.getClientsOf(id);
    }

    @GetMapping("/trainer/{id}/adherence")
    public Map<String, Double> adherence(@PathVariable("id") String id) {
        return progressService.weeklyAdherenceByClient(id);
    }

    @PostMapping("/plans")
    public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.create(plan));
    }

    @GetMapping("/plans")
    public List<Plan> listPlans(@RequestParam("trainerId") String trainerId) {
        return planService.listByTrainer(trainerId);
    }

    @GetMapping("/plans/{id}")
    public Plan getPlan(@PathVariable("id") String id) {
        return planService.get(id);
    }

    @PutMapping("/plans/{id}")
    public Plan updatePlan(@PathVariable("id") String id, @RequestBody Plan plan) {
        return planService.update(id, plan);
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable("id") String id) {
        planService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
