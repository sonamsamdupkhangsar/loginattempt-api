package me.sonam.attempt.springboot;


import me.sonam.attempt.service.AttemptHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class Router {
    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    @Bean
    public RouterFunction<ServerResponse> route(AttemptHandler handler) {
        LOG.info("building router function");
        return RouterFunctions
                .route(PUT("/attempts/login/failed"), handler::loginFailed)
                .andRoute(PUT("/attempts/login/success"), handler::loginSuccess);

    }
}