package net.lop223.helpdesksystem.service.userclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class UserServiceClient {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final RestClient restClient;
    private final String apiToken;

    public UserServiceClient(
            RestClient.Builder builder,
            @Value("${services.user.base-url}") String baseUrl,
            @Value("${services.user.api-token}") String apiToken
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(4));

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.apiToken = apiToken;
    }

    public UserReference getById(Long id) {
        return restClient.get()
                .uri("/internal/users/{id}", id)
                .header(INTERNAL_TOKEN_HEADER, apiToken)
                .retrieve()
                .body(UserReference.class);
    }

    public UserReference getByUsername(String username) {
        return restClient.get()
                .uri("/internal/users/by-username/{username}", username)
                .header(INTERNAL_TOKEN_HEADER, apiToken)
                .retrieve()
                .body(UserReference.class);
    }

    public record UserReference(Long id, String username) {
    }
}