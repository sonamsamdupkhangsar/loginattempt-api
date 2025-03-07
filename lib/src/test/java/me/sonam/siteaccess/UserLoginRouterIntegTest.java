package me.sonam.siteaccess;

import me.sonam.siteaccess.persist.entity.UserLogin;
import me.sonam.siteaccess.persist.repo.UserLoginRepository;
import me.sonam.siteaccess.springboot.Application;
import me.sonam.siteaccess.springboot.TestConfig;
import me.sonam.webclients.friendship.SeUserFriend;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * this will test the end-to-end from the Router to business service to entity persistence using in-memory db.
 */
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class UserLoginRouterIntegTest {
    private static final Logger LOG = LoggerFactory.getLogger(UserLoginRouterIntegTest.class);

    private static MockWebServer mockWebServer;
    @MockitoBean
    ReactiveJwtDecoder jwtDecoder;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    ApplicationContext context;

    @Autowired
    private UserLoginRepository userLoginRepository;

  //  @org.junit.jupiter.api.BeforeEach
    public void setup() {
        this.webTestClient = WebTestClient
                .bindToApplicationContext(this.context)
                // add Spring Security test Support
                .apply(springSecurity())
                .configureClient()
                //   .filter(basicAuthentication("user", "password"))
                .build();
    }
    @BeforeAll
    static void setupMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        LOG.info("host: {}, port: {}", mockWebServer.getHostName(), mockWebServer.getPort());
    }

    @AfterAll
    public static void shutdownMockWebServer() throws IOException {
        LOG.info("shutdown and close mockWebServer");
        mockWebServer.shutdown();
        mockWebServer.close();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) throws IOException {
    }

    // login success will have a token so pass it
    @Test
    public void loginSuccess() throws InterruptedException {
        LOG.info("request friendship test");

        LocalDateTime localDateTime = LocalDateTime.now().minusSeconds(1);

        final String ipAddress = "1.0.0.28";

        UserLogin userLogin = new UserLogin("lazybody62", null, ipAddress, UserLogin.Status.FAILED.name(), localDateTime);
        userLoginRepository.save(userLogin).subscribe();

        userLogin.setAttemptCount(2);
        userLoginRepository.save(userLogin).subscribe();

        UUID userId = UUID.fromString("5d8de63a-0b45-4c33-b9eb-d7fb8d662107");
        final String authenticationId = "dave";
        Jwt jwt = jwt(authenticationId, userId);
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("userId", UUID.randomUUID().toString());
        bodyMap.put("username", userLogin.getId());
        bodyMap.put("ipAddress", userLogin.getIp());

        EntityExchangeResult<String> entityExchangeResult = webTestClient.
                put().uri("/access/login/success").bodyValue(bodyMap)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk().expectBody(String.class)
                .returnResult();

        assert userLogin.getId() != null;
        Mono<UserLogin> userLoginMono = userLoginRepository.findById(userLogin.getId());
        StepVerifier.create(userLoginMono).expectNextCount(1).verifyComplete();

        userLoginMono = userLoginRepository.findById(userLogin.getId());
        StepVerifier.create(userLoginMono).assertNext(userLogin1 -> {
            LOG.info("assert the status flag and the ipaddress with username and userid: {}", userLogin1);
            assertThat(userLogin1.getId()).isEqualTo(userLogin.getId());
            assertThat(userLogin1.getUserId()).isEqualTo(userLogin.getUserId());
            assertThat(userLogin1.getIp()).isEqualTo(userLogin.getIp());
            assertThat(userLogin1.getAttemptCount()).isEqualTo(0); //reset to 0 on success
        }).verifyComplete();

        LOG.info("after a success do a failed login attempt but should continue to allow another attempt");
        EntityExchangeResult<Map<String, String>> entityExchangeResult2 = webTestClient.
                put().uri("/access/login/failed").bodyValue(bodyMap)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk().expectBody(new ParameterizedTypeReference<Map<String, String>>(){})
                .returnResult();

        assertThat(entityExchangeResult2).isNotNull();
        assertThat(entityExchangeResult2.getResponseBody().get("message")).isEqualTo("CONTINUE");

        assert userLogin.getId() != null;
        userLoginMono = userLoginRepository.findById(userLogin.getId());
        StepVerifier.create(userLoginMono).expectNextCount(1).verifyComplete();

        userLoginMono = userLoginRepository.findById(userLogin.getId());
        StepVerifier.create(userLoginMono).assertNext(userLogin1 -> {
            LOG.info("assert the status flag and the ipaddress with username and userid: {}", userLogin1);
            assertThat(userLogin1.getId()).isEqualTo(userLogin.getId());
            assertThat(userLogin1.getUserId()).isEqualTo(userLogin.getUserId());
            assertThat(userLogin1.getIp()).isEqualTo(userLogin.getIp());
            assertThat(userLogin1.getAttemptCount()).isEqualTo(1); //reset to 0 on success
        }).verifyComplete();


    }

    @Test
    public void loginFailed() throws InterruptedException {
        LOG.info("loginFailed test");

        UUID userId = UUID.fromString("5d8de63a-0b45-4c33-b9eb-d7fb8d662107");
        final String authenticationId = "dave";
        Jwt jwt = jwt(authenticationId, userId);
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));
        Map<String, String> bodyMap = new HashMap<>();
        UUID uuid = UUID.randomUUID();

        bodyMap.put("userId", uuid.toString());
        bodyMap.put("username", "lazybody6");
        bodyMap.put("ipAddress", "1.0.0.26");

        LOG.info("attempt 1");
        webTestClient.
                put().uri("/access/login/failed").bodyValue(bodyMap)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk()
                        .returnResult(Map.class).getResponseBody().next()
                    .map(responseMap -> {
                        LOG.info("responseMap contains {}", responseMap);
                            assertThat(responseMap.containsKey("message")).isTrue();
                            assertThat(responseMap.get("message")).isEqualTo("CONTINUE");
                            return responseMap;
                }).block();



        Mono<UserLogin> userLoginMono = userLoginRepository.findById("lazybody6");
        StepVerifier.create(userLoginMono).expectNextCount(1).verifyComplete();

        userLoginMono = userLoginRepository.findById("lazybody6");
        StepVerifier.create(userLoginMono).assertNext(userLogin1 -> {
            LOG.info("assert the status flag and the ipaddress with username and userid: {}", userLogin1);
            assertThat(userLogin1.getId()).isEqualTo("lazybody6");
            assertThat(userLogin1.getUserId()).isEqualTo(uuid);
            assertThat(userLogin1.getIp()).isEqualTo("1.0.0.26");
            assertThat(userLogin1.getAttemptCount()).isEqualTo(1); //1st login failed attempt
        }).verifyComplete();


        LOG.info("record 2nd login failed attempt");
        webTestClient.
                put().uri("/access/login/failed").bodyValue(bodyMap)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk()
                .returnResult(Map.class).getResponseBody().next()
                .map(responseMap -> {
                    LOG.info("responseMap contains {}", responseMap);
                    assertThat(responseMap.containsKey("message")).isTrue();
                    assertThat(responseMap.get("message")).isEqualTo("CONTINUE");
                    return responseMap;
                }).block();



        userLoginMono = userLoginRepository.findById("lazybody6");
        StepVerifier.create(userLoginMono).expectNextCount(1).verifyComplete();

        userLoginMono = userLoginRepository.findById("lazybody6");
        StepVerifier.create(userLoginMono).assertNext(userLogin1 -> {
            LOG.info("assert the status flag and the ipaddress with username and userid: {}", userLogin1);
            assertThat(userLogin1.getId()).isEqualTo("lazybody6");
            assertThat(userLogin1.getUserId()).isEqualTo(uuid);
            assertThat(userLogin1.getIp()).isEqualTo("1.0.0.26");
            assertThat(userLogin1.getAttemptCount()).isEqualTo(2);
        }).verifyComplete();

        LOG.info("record 3rd login failed attempt, which should give LOCK_USER_OUT");
        webTestClient.
                put().uri("/access/login/failed").bodyValue(bodyMap)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk()
                .returnResult(Map.class).getResponseBody().next()
                .map(responseMap -> {
                    LOG.info("responseMap contains {}", responseMap);
                    assertThat(responseMap.containsKey("message")).isTrue();
                    assertThat(responseMap.get("message")).isEqualTo("LOCK_USER_OUT");
                    return responseMap;
                }).block();

        userLoginMono = userLoginRepository.findById("lazybody6");
        StepVerifier.create(userLoginMono).expectNextCount(1).verifyComplete();

        userLoginMono = userLoginRepository.findById("lazybody6");
        StepVerifier.create(userLoginMono).assertNext(userLogin1 -> {
            LOG.info("assert the status flag and the ipaddress with username and userid: {}", userLogin1);
            assertThat(userLogin1.getId()).isEqualTo("lazybody6");
            assertThat(userLogin1.getUserId()).isEqualTo(uuid);
            assertThat(userLogin1.getIp()).isEqualTo("1.0.0.26");
            assertThat(userLogin1.getAttemptCount()).isEqualTo(3);
        }).verifyComplete();

        LOG.info("sleep for 8 seconds");
        Thread.sleep(8000);

        //test for reset of userAttempt which should delete the userLogin row and create a new one
        LOG.info("after 3 seconds do another login failed attempt, which should give CONTINUE");
        webTestClient.
                put().uri("/access/login/failed").bodyValue(bodyMap)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk()
                .returnResult(Map.class).getResponseBody().next()
                .map(responseMap -> {
                    LOG.info("responseMap contains {}", responseMap);
                    assertThat(responseMap.containsKey("message")).isTrue();
                    assertThat(responseMap.get("message")).isEqualTo("CONTINUE");
                    return responseMap;
                }).block();



        userLoginMono = userLoginRepository.findById("lazybody6");
        StepVerifier.create(userLoginMono).expectNextCount(1).verifyComplete();

        userLoginMono = userLoginRepository.findById("lazybody6");
        StepVerifier.create(userLoginMono).assertNext(userLogin1 -> {
            LOG.info("assert the status flag and the ipaddress with username and userid: {}", userLogin1);
            assertThat(userLogin1.getId()).isEqualTo("lazybody6");
            assertThat(userLogin1.getUserId()).isEqualTo(uuid);
            assertThat(userLogin1.getIp()).isEqualTo("1.0.0.26");
            assertThat(userLogin1.getAttemptCount()).isEqualTo(1);
        }).verifyComplete();
    }

    private Jwt jwt(String subjectName, UUID userId) {
        return new Jwt("token", null, null,
                Map.of("alg", "none"), Map.of("sub", subjectName, "userId", userId.toString()));
    }

    private Consumer<HttpHeaders> addJwt(Jwt jwt) {
        return headers -> headers.setBearerAuth(jwt.getTokenValue());
    }
}
