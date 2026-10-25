package com.fittrack.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    private final MongoTemplate mongoTemplate;

    public TestController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("app", "FitTrack");
        response.put("server", "running");
        try {
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
            response.put("mongodb", "connected");
            response.put("database", mongoTemplate.getDb().getName());
        } catch (Exception e) {
            response.put("mongodb", "unreachable");
        }
        return response;
    }
}
