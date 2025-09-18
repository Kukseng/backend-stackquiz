package kh.edu.cstad.stackquizapi.security;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for creating a {@link Keycloak} admin client bean.
 * <p>
 * This client is configured to connect to a specific Keycloak server and realm
 * using client credentials, enabling administrative operations such as managing users
 * and roles programmatically.
 * </p>
 *
 * <strong>Note:</strong> In production, sensitive values like {@code clientSecret}
 * should be stored securely (e.g., environment variables, Vault) rather than hard-coded.
 *
 * @author PECH RATTANAKMONY
 */
@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.realm}")
    private String realmName;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Bean
    public Keycloak keycloak() {

        return KeycloakBuilder
                .builder()
                .realm(realmName)
                .serverUrl(serverUrl)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

    }

}
