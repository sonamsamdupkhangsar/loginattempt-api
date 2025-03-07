package me.sonam.siteaccess.persist.repo;

import me.sonam.siteaccess.persist.entity.UserLogin;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserLoginRepository extends ReactiveCrudRepository<UserLogin, String> {
    Mono<Integer> countByUsername(String username);
}
