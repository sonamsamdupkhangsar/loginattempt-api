package me.sonam.siteaccess.service;

import me.sonam.siteaccess.FailedLoginException;
import me.sonam.siteaccess.persist.entity.UserLogin;
import me.sonam.siteaccess.persist.repo.UserLoginRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SiteAccessLogger implements SiteAccessLog {
    private static final Logger LOG = LoggerFactory.getLogger(SiteAccessLogger.class);

    public enum Login {
        CONTINUE, LOCK_USER_OUT
    }
    public enum MaxAccess {
        MAX_ACCESS, NOT_MAX_ACCESS
    }
    @Value("${lockOnFailedLoginAttempts}")
    private int lockOnFailedLoginAttempts;

    @Value("${maxAccessWithinSeconds}")
    private int maxAccessWithinSeconds;

    @Value("${resetAttemptIntervalInSeconds}")
    private int resetAttemptIntervalInSeconds;

    private final UserLoginRepository userLoginRepository;

    public SiteAccessLogger(UserLoginRepository userLoginRepository) {
        this.userLoginRepository = userLoginRepository;
    }

    @Override
    public Mono<String> loginFailed(String username, UUID userId, String ipAddress, LocalDateTime dateTime) {
        LOG.info("checking if max number of tries reached in login failed");
        return userLoginRepository.findById(username)
                .switchIfEmpty(Mono.just(new UserLogin(username, userId, ipAddress, UserLogin.Status.FAILED.name(), dateTime)))
                .flatMap(userLogin -> {
                    // this flatmap will delete the userLogin (essentially reset the user login attempt) after an hour interval
                    LocalDateTime now = LocalDateTime.now();
                    Duration duration = Duration.between(userLogin.getDateTime(), now);

                    if (userLogin.getId() == null) {
                        LOG.error("userLogin.id is null");
                        return Mono.error(new FailedLoginException("userLogin.id cannot be null"));
                    }
                    LOG.info("userLogin.getDateTime {}, secondsAfter {}", userLogin.getDateTime(), now);
                    if (duration.getSeconds() >= resetAttemptIntervalInSeconds){
                        LOG.info("reset user login attempt after {} seconds", resetAttemptIntervalInSeconds);
                        return userLoginRepository.deleteById(userLogin.getId()).thenReturn(
                                new UserLogin(username, userId, ipAddress, UserLogin.Status.FAILED.name(), dateTime));
                    }
                    else {
                        LOG.info("user attempt is not after {} seconds, no need to delete.", resetAttemptIntervalInSeconds);
                        return Mono.just(userLogin);
                    }
                })

                .flatMap(userLogin -> {
                    userLogin.incrementAttemptCount();
                    return userLoginRepository.save(userLogin);
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

        return userLoginRepository.findById(username)
                .doOnNext(userLogin -> LOG.info("found userLogin before deletion on success {}", userLogin))
                .doOnNext(UserLogin::loginSuccess)
                .flatMap(userLoginRepository::save)
                .thenReturn(Login.CONTINUE.name());
    }

}

