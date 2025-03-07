package me.sonam.siteaccess.service;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface SiteAccessHandler {
    Mono<ServerResponse> loginFailed(ServerRequest serverRequest);
    Mono<ServerResponse> loginSuccess(ServerRequest serverRequest);
}
