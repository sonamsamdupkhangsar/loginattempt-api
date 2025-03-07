package me.sonam.attempt.service;

import me.sonam.attempt.LoginAttemptException;
import me.sonam.attempt.persist.entity.LoginAttempt;
import me.sonam.attempt.persist.repo.LoginAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoginAttemptLogger implements AttemptLog {
    private static final Logger LOG = LoggerFactory.getLogger(LoginAttemptLogger.class);

    public enum Login {
        CONTINUE, LOCK_USER_OUT
    }

    @Value("${lockOnFailedLoginAttempts}")
    private int lockOnFailedLoginAttempts;

    @Value("${maxAccessWithinSeconds}")
    private int maxAccessWithinSeconds;

    @Value("${resetAttemptIntervalInSeconds}")
    private int resetAttemptIntervalInSeconds;

    private final LoginAttemptRepository loginAttemptRepository;

    public LoginAttemptLogger(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Override
    public Mono<String> loginFailed(String username, UUID userId, String ipAddress, LocalDateTime dateTime) {
        LOG.info("checking if max number of tries reached in login failed");
        return loginAttemptRepository.findById(username)
                .switchIfEmpty(Mono.just(new LoginAttempt(username, userId, ipAddress, LoginAttempt.Status.FAILED.name(), dateTime)))
                .flatMap(userLogin -> {
                    // this flatmap will delete the userLogin (essentially reset the user login attempt) after an hour interval
                    LocalDateTime now = LocalDateTime.now();
                    Duration duration = Duration.between(userLogin.getDateTime(), now);

                    if (userLogin.getId() == null) {
                        LOG.error("userLogin.id is null");
                        return Mono.error(new LoginAttemptException("userLogin.id cannot be null"));
                    }
                    LOG.info("userLogin.getDateTime {}, secondsAfter {}", userLogin.getDateTime(), now);
                    if (duration.getSeconds() >= resetAttemptIntervalInSeconds){
                        LOG.info("reset user login attempt after {} seconds", resetAttemptIntervalInSeconds);
                        return loginAttemptRepository.deleteById(userLogin.getId()).thenReturn(
                                new LoginAttempt(username, userId, ipAddress, LoginAttempt.Status.FAILED.name(), dateTime));
                    }
                    else {
                        LOG.info("user attempt is not after {} seconds, no need to delete.", resetAttemptIntervalInSeconds);
                        return Mono.just(userLogin);
                    }
                })

                .flatMap(userLogin -> {
                    userLogin.incrementAttemptCount();
                    return loginAttemptRepository.save(userLogin);
                })
                .flatMap(userLogin -> {
                    if (userLogin.getAttemptCount() >= lockOnFailedLoginAttempts) {
                        LOG.info("max attempt reached, lockout user");
                        return Mono.just(Login.LOCK_USER_OUT.name());
                    }
                    else {
                        LOG.info("max attempt NOT reached, continue");
                        return Mono.just(Login.CONTINUE.name());
                    }
        });
    }

    @Override
    public Mono<String> loginSuccess(String username, UUID userId, String ipAddress, LocalDateTime localDateTime) {
        LOG.info("entering login success record");

        return loginAttemptRepository.findById(username)
                .doOnNext(userLogin -> LOG.info("found userLogin before deletion on success {}", userLogin))
                .doOnNext(LoginAttempt::loginSuccess)
                .flatMap(loginAttemptRepository::save)
                .thenReturn(Login.CONTINUE.name());
    }

}

