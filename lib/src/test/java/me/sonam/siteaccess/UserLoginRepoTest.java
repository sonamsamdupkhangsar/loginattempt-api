package me.sonam.siteaccess;

import me.sonam.siteaccess.persist.entity.UserLogin;
import me.sonam.siteaccess.persist.repo.UserLoginRepository;
import me.sonam.siteaccess.springboot.Application;
import me.sonam.siteaccess.springboot.TestConfig;
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
public class UserLoginRepoTest {
	private static final Logger LOG = LoggerFactory.getLogger(UserLoginRepoTest.class);

	@Autowired
	private UserLoginRepository userLoginRepository;

	@Test
	public void failedLogin() {
		LocalDateTime localDateTime = LocalDateTime.now().minusSeconds(1);

		final String ipAddress = "{\"ip\": \"1.0.0.28\"}";

		UserLogin userLogin = new UserLogin("lazybody62", null, ipAddress, UserLogin.Status.FAILED.name(), localDateTime);
		userLoginRepository.save(userLogin).subscribe();

		userLoginRepository.countByUsername("lazybody62").subscribe(integer ->
				LOG.info("found {} rows matching username and ipaddress", integer));


	}
}
