package me.sonam.siteaccess.service;

import me.sonam.siteaccess.persist.entity.UserLogin;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SiteAccessLog {
    Mono<String> loginFailed(String username, UUID userId, String ipAddress, LocalDateTime localDateTime);
    Mono<String> loginSuccess(String username, UUID userId, String ipAddress, LocalDateTime localDateTime);
}
