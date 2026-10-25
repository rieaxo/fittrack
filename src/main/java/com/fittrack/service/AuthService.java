package com.fittrack.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fittrack.dto.LoginRequest;
import com.fittrack.dto.RegisterRequest;
import com.fittrack.exception.AuthenticationException;
import com.fittrack.exception.InvalidInputException;
import com.fittrack.exception.UserNotFoundException;
import com.fittrack.model.Client;
import com.fittrack.model.Trainer;
import com.fittrack.model.User;
import com.fittrack.repository.UserRepository;
import com.fittrack.util.PasswordUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest request) {
        if (request == null || request.role() == null) {
            throw new InvalidInputException("Role is required");
        }
        if (request.password() == null || request.password().length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters");
        }
        String hash = PasswordUtil.sha256(request.password());

        User user = switch (request.role().toUpperCase()) {
            case "TRAINER" -> new Trainer(request.name(), request.email(), hash);
            case "CLIENT" -> {
                if (request.trainerId() == null || request.trainerId().isBlank()) {
                    throw new InvalidInputException("Please choose a trainer");
                }
                getTrainer(request.trainerId());
                yield new Client(request.name(), request.email(), hash, request.trainerId());
            }
            default -> throw new InvalidInputException("Role must be TRAINER or CLIENT");
        };

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new InvalidInputException("This email is already registered");
        }
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        if (request == null || request.email() == null || request.password() == null) {
            throw new AuthenticationException("Invalid email or password");
        }
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
        if (!user.getPasswordHash().equals(PasswordUtil.sha256(request.password()))) {
            throw new AuthenticationException("Invalid email or password");
        }
        return user;
    }

    public List<User> getTrainers() {
        return userRepository.findAllTrainers();
    }

    public Trainer getTrainer(String id) {
        return userRepository.findById(id)
                .filter(Trainer.class::isInstance)
                .map(Trainer.class::cast)
                .orElseThrow(() -> new UserNotFoundException("Trainer not found"));
    }

    public Client getClient(String id) {
        return userRepository.findById(id)
                .filter(Client.class::isInstance)
                .map(Client.class::cast)
                .orElseThrow(() -> new UserNotFoundException("Client not found"));
    }

    public List<Client> getClientsOf(String trainerId) {
        getTrainer(trainerId);
        return userRepository.findClientsByTrainerId(trainerId).stream()
                .filter(Client.class::isInstance)
                .map(Client.class::cast)
                .toList();
    }
}
