package me.sonam.attempt.service;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class LoginAttemptWebHandler implements AttemptHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LoginAttemptWebHandler.class);

    private AttemptLog attemptLog;

    public LoginAttemptWebHandler(AttemptLog attemptLog) {
        this.attemptLog = attemptLog;
    }

    @Override
    public Mono<ServerResponse> loginFailed(ServerRequest serverRequest) {//String username, UUID userId, String ipAddress) {
        LOG.info("record login failed");

        return serverRequest.bodyToMono(Map.class).flatMap(map -> {
            final String username = map.get("username").toString();
            UUID userId = null;
            if (map.get("userId") != null) {
                userId = UUID.fromString(map.get("userId").toString());
            }
            final String ipAddress = map.get("ipAddress").toString();
            return attemptLog.loginFailed(username, userId, ipAddress, LocalDateTime.now());
        })
                .flatMap(s ->  ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.debug("exception occurred in isFriends method", throwable);
                    LOG.error("failed in isFriends method {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "isFriends() method failed with error: " + throwable.getMessage()));
                });
    }

    @Override
    public Mono<ServerResponse> loginSuccess(ServerRequest serverRequest) {
        LOG.info("record login success");

        return serverRequest.bodyToMono(Map.class).flatMap(map -> {
                    final String username = map.get("username").toString();
                    UUID userId = null;
                    if (map.get("userId") != null) {
                        userId = UUID.fromString(map.get("userId").toString());
                    }
                    final String ipAddress = map.get("ipAddress").toString();
                    LOG.info("username: {}, userId: {}, ipAddress: {}", username, userId, ipAddress);

                    return attemptLog.loginSuccess(username, userId, ipAddress, LocalDateTime.now());
                })
                .flatMap(s ->  ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.debug("exception occurred in isFriends method", throwable);
                    LOG.error("failed in isFriends method {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "isFriends() method failed with error: " + throwable.getMessage()));
                });
    }

}
