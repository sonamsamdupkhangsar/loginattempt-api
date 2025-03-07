package me.sonam.attempt.service;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AttemptLog {
    Mono<String> loginFailed(String username, UUID userId, String ipAddress, LocalDateTime localDateTime);
    Mono<String> loginSuccess(String username, UUID userId, String ipAddress, LocalDateTime localDateTime);
}
