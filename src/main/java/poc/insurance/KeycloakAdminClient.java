package poc.insurance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class KeycloakAdminClient {

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    private WebClient getWebClient() {
        return WebClient.builder().baseUrl(keycloakServerUrl).build();
    }

    private String getAdminAccessToken() {
        String tokenEndpoint = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakServerUrl, realm);

        Map<String, Object> response = getWebClient().post()
                .uri(tokenEndpoint)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("grant_type=client_credentials" +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("Token admin non ricevuto da Keycloak");
        }

        return response.get("access_token").toString();
    }

    public void terminateUserSessions(String userId) {
        String token = getAdminAccessToken();

        String logoutUrl = String.format("%s/admin/realms/%s/users/%s/logout", keycloakServerUrl, realm, userId);

        getWebClient().post()
                .uri(logoutUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public String getUserIdByUsername(String username) {
        String token = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/users?username=%s", keycloakServerUrl, realm, username);

        List<Map<String, Object>> result = getWebClient().get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        if (result == null || result.isEmpty()) {
            throw new RuntimeException("Utente non trovato in Keycloak: " + username);
        }

        return result.get(0).get("id").toString();
    }

}
