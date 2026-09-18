package hr.fer.hydro.config.swagger;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupConfig {

    @Bean
    public GroupedOpenApi allOpenApi() {
        final String[] paths = {"/**"};
        return GroupedOpenApi
                .builder()
                .group("*")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi authOpenApi() {
        final String[] paths = {"/auth/**", "/google-2fa/**"};
        return GroupedOpenApi
                .builder()
                .group("Auth")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi stationsOpenApi() {
        final String[] paths = {"/stations/**"};
        return GroupedOpenApi
                .builder()
                .group("Stations")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi measurementsOpenApi() {
        final String[] paths = {"/measurements/**"};
        return GroupedOpenApi
                .builder()
                .group("Measurements")
                .pathsToMatch(paths)
                .build();
    }
}
