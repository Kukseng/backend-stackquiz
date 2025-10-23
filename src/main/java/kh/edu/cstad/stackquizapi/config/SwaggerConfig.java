package kh.edu.cstad.stackquizapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

//    @Value("${spring.app.api-url}")
    @Value("http://localhost:9999")
    private String apiUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("StackQuiz API")
                        .description("Quiz Platform API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("StackQuiz")
                                .email("info.stackquiz@gmail.com")
                                .url("https://stackquiz.me"))
                        .license(new License()

                        ))
                .servers(List.of(
                        new Server().url(apiUrl).description("Development server"),
                        new Server().url("https://stackquiz-api.stackquiz.me").description("Production server")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Bearer token authentication. " +
                                                     "Get your token from the /api/v1/auth/login endpoint.")));
    }
}