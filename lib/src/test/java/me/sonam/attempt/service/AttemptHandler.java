package me.sonam.attempt.service;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface AttemptHandler {
    Mono<ServerResponse> loginFailed(ServerRequest serverRequest);
    Mono<ServerResponse> loginSuccess(ServerRequest serverRequest);
}
