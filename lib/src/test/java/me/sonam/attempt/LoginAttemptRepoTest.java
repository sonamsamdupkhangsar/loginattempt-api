package me.sonam.attempt;

import me.sonam.attempt.persist.entity.LoginAttempt;
import me.sonam.attempt.persist.repo.LoginAttemptRepository;
import me.sonam.attempt.springboot.Application;
import me.sonam.attempt.springboot.TestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, TestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class LoginAttemptRepoTest {
	private static final Logger LOG = LoggerFactory.getLogger(LoginAttemptRepoTest.class);

	@Autowired
	private LoginAttemptRepository loginAttemptRepository;

	@Test
	public void failedLogin() {
		LocalDateTime localDateTime = LocalDateTime.now().minusSeconds(1);

		final String ipAddress = "{\"ip\": \"1.0.0.28\"}";

		LoginAttempt loginAttempt = new LoginAttempt("lazybody62", null, ipAddress, LoginAttempt.Status.FAILED.name(), localDateTime);
		loginAttemptRepository.save(loginAttempt).subscribe();

		loginAttemptRepository.countByUsername("lazybody62").subscribe(integer ->
				LOG.info("found {} rows matching username and ipaddress", integer));


	}
}
