package com.queueless.queueless.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI queueLessOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("QueueLess API")
                        .description("Backend API for smart hospital queue and appointment management.")
                        .version("v1")
                        .contact(new Contact().name("QueueLess").email("support@queueless.local"))
                        .license(new License().name("Internal project use")));
    }
}
