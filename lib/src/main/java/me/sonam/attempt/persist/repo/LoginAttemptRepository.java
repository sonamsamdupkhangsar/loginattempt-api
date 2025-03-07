package me.sonam.attempt.persist.repo;

import me.sonam.attempt.persist.entity.LoginAttempt;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoginAttemptRepository extends ReactiveCrudRepository<LoginAttempt, String> {
    Mono<Integer> countByUsername(String username);
}
