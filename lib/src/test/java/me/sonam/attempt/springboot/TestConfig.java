package me.sonam.attempt.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

//@Configuration
public class TestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(TestConfig.class);

    @Bean
    public WebClient.Builder webClientBuilder() {
        LOG.info("returning non-load balanced webclient part");
        return WebClient.builder();
    }
}
