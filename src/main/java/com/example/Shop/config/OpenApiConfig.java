package com.example.Shop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plumbing Store API")
                        .description("REST API for the plumbing store web application. Manages products, categories, cart, orders, and user authentication.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Support")
                                .email("support@plumbingshop.example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
