package kh.edu.cstad.stackquizapi.security;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
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

    @Bean
    public Keycloak keycloak() {

        return KeycloakBuilder
                .builder()
                .realm("sq-api")
                .serverUrl("http://localhost:8888")
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId("admin-cli")
                .clientSecret("T201k8DBviE5iubvd74oUnHRFqRmJ3en")
                .build();

    }

}
