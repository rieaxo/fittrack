package com.fittrack.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fittrack.dto.LogRequest;
import com.fittrack.model.Plan;
import com.fittrack.model.ProgressLog;
import com.fittrack.service.PlanService;
import com.fittrack.service.ProgressService;

@RestController
@RequestMapping("/api/clients/{id}")
public class ClientController {

    private final PlanService planService;
    private final ProgressService progressService;

    public ClientController(PlanService planService, ProgressService progressService) {
        this.planService = planService;
        this.progressService = progressService;
    }

    @GetMapping("/plan")
    public ResponseEntity<Plan> plan(@PathVariable("id") String id) {
        return planService.getPlanForClient(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/logs")
    public ResponseEntity<ProgressLog> saveLog(@PathVariable("id") String id, @RequestBody LogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(progressService.saveLog(id, request));
    }

    @GetMapping("/logs")
    public List<ProgressLog> logs(@PathVariable("id") String id) {
        return progressService.getLogs(id);
    }

    @GetMapping("/adherence")
    public Map<String, Object> adherence(@PathVariable("id") String id) {
        return progressService.getAdherence(id);
    }
}
