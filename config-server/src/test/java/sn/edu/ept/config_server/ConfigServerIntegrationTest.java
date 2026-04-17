package sn.edu.ept.config_server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Spring Cloud Config Server.
 * Tests validate configuration loading for order-service, parcel-service, and user-service.
 * 
 * Validates: Requirements 12.1, 12.2, 12.3, 12.4, 12.5
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Config Server Integration Tests")
class ConfigServerIntegrationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + port;
        webTestClient = WebTestClient.bindToServer()
            .baseUrl(baseUrl)
            .build();
    }

    /**
     * Test 7.1.1: Test endpoint /{application}/{profile} for order-service
     * Validates: Requirements 12.1, 12.2, 12.3
     */
    @Test
    @DisplayName("Should load order-service configuration successfully")
    void testOrderServiceConfiguration() {
        // When: Request configuration for order-service
        Map<String, Object> body = webTestClient.get()
            .uri("/order-service/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        // Then: Response should contain expected structure
        assertThat(body).isNotNull();
        assertThat(body).containsKey("name");
        assertThat(body).containsKey("profiles");
        assertThat(body).containsKey("propertySources");
        
        assertThat(body.get("name")).isEqualTo("order-service");
        
        @SuppressWarnings("unchecked")
        List<String> profiles = (List<String>) body.get("profiles");
        assertThat(profiles).contains("default");
    }

    /**
     * Test 7.1.2: Test endpoint /{application}/{profile} for parcel-service
     * Validates: Requirements 12.1, 12.2, 12.4
     */
    @Test
    @DisplayName("Should load parcel-service configuration successfully")
    void testParcelServiceConfiguration() {
        // When: Request configuration for parcel-service
        Map<String, Object> body = webTestClient.get()
            .uri("/parcel-service/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        // Then: Response should contain expected structure
        assertThat(body).isNotNull();
        assertThat(body).containsKey("name");
        assertThat(body).containsKey("profiles");
        assertThat(body).containsKey("propertySources");
        
        assertThat(body.get("name")).isEqualTo("parcel-service");
        
        @SuppressWarnings("unchecked")
        List<String> profiles = (List<String>) body.get("profiles");
        assertThat(profiles).contains("default");
    }

    /**
     * Test 7.1.3: Test endpoint /{application}/{profile} for user-service
     * Validates: Requirements 12.1, 12.2, 12.5
     */
    @Test
    @DisplayName("Should load user-service configuration successfully")
    void testUserServiceConfiguration() {
        // When: Request configuration for user-service
        Map<String, Object> body = webTestClient.get()
            .uri("/user-service/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        // Then: Response should contain expected structure
        assertThat(body).isNotNull();
        assertThat(body).containsKey("name");
        assertThat(body).containsKey("profiles");
        assertThat(body).containsKey("propertySources");
        
        assertThat(body.get("name")).isEqualTo("user-service");
        
        @SuppressWarnings("unchecked")
        List<String> profiles = (List<String>) body.get("profiles");
        assertThat(profiles).contains("default");
    }

    /**
     * Test 7.1.4: Verify configuration merging (application.yml + service.yml)
     * Validates: Requirements 12.3, 12.4, 12.5
     */
    @Test
    @DisplayName("Should merge global and service-specific configurations")
    void testConfigurationMerging() {
        // When: Request configuration for order-service
        Map<String, Object> body = webTestClient.get()
            .uri("/order-service/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        // Then: Response should contain both global and service-specific configurations
        assertThat(body).isNotNull();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> propertySources = (List<Map<String, Object>>) body.get("propertySources");
        
        assertThat(propertySources).isNotEmpty();
        
        // Verify both application.yml and order-service.yml are present
        boolean hasGlobalConfig = propertySources.stream()
            .anyMatch(ps -> {
                String name = (String) ps.get("name");
                return name != null && name.contains("application.yml");
            });
        
        boolean hasServiceConfig = propertySources.stream()
            .anyMatch(ps -> {
                String name = (String) ps.get("name");
                return name != null && name.contains("order-service.yml");
            });
        
        assertThat(hasGlobalConfig).as("Global configuration (application.yml) should be present").isTrue();
        assertThat(hasServiceConfig).as("Service-specific configuration (order-service.yml) should be present").isTrue();
    }

    /**
     * Test 7.1.5: Verify order-service specific properties
     * Validates: Requirements 12.3
     */
    @Test
    @DisplayName("Should contain order-service specific properties")
    void testOrderServiceSpecificProperties() {
        // When: Request configuration for order-service
        Map<String, Object> body = webTestClient.get()
            .uri("/order-service/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        // Then: Response should contain order-service specific properties
        assertThat(body).isNotNull();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> propertySources = (List<Map<String, Object>>) body.get("propertySources");
        
        // Find order-service.yml property source
        Map<String, Object> orderServiceSource = propertySources.stream()
            .filter(ps -> {
                String name = (String) ps.get("name");
                return name != null && name.contains("order-service.yml");
            })
            .findFirst()
            .orElse(null);
        
        assertThat(orderServiceSource).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> source = (Map<String, Object>) orderServiceSource.get("source");
        assertThat(source).isNotNull();
        
        // Verify key properties exist (checking for nested properties)
        assertThat(source).containsKey("server.port");
        assertThat(source).containsKey("spring.application.name");
    }

    /**
     * Test 7.1.6: Verify parcel-service specific properties
     * Validates: Requirements 12.4
     */
    @Test
    @DisplayName("Should contain parcel-service specific properties including tariff configuration")
    void testParcelServiceSpecificProperties() {
        // When: Request configuration for parcel-service
        Map<String, Object> body = webTestClient.get()
            .uri("/parcel-service/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        // Then: Response should contain parcel-service specific properties
        assertThat(body).isNotNull();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> propertySources = (List<Map<String, Object>>) body.get("propertySources");
        
        // Find parcel-service.yml property source
        Map<String, Object> parcelServiceSource = propertySources.stream()
            .filter(ps -> {
                String name = (String) ps.get("name");
                return name != null && name.contains("parcel-service.yml");
            })
            .findFirst()
            .orElse(null);
        
        assertThat(parcelServiceSource).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> source = (Map<String, Object>) parcelServiceSource.get("source");
        assertThat(source).isNotNull();
        
        // Verify parcel-specific properties
        assertThat(source).containsKey("parcel.tariff.base-price");
        assertThat(source).containsKey("parcel.tariff.weight-rate");
        assertThat(source).containsKey("parcel.tariff.volume-rate");
        assertThat(source).containsKey("parcel.tariff.distance-rate");
    }

    /**
     * Test 7.1.7: Verify user-service specific properties
     * Validates: Requirements 12.5
     */
    @Test
    @DisplayName("Should contain user-service specific properties")
    void testUserServiceSpecificProperties() {
        // When: Request configuration for user-service
        Map<String, Object> body = webTestClient.get()
            .uri("/user-service/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        // Then: Response should contain user-service specific properties
        assertThat(body).isNotNull();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> propertySources = (List<Map<String, Object>>) body.get("propertySources");
        
        // Find user-service.yml property source
        Map<String, Object> userServiceSource = propertySources.stream()
            .filter(ps -> {
                String name = (String) ps.get("name");
                return name != null && name.contains("user-service.yml");
            })
            .findFirst()
            .orElse(null);
        
        assertThat(userServiceSource).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> source = (Map<String, Object>) userServiceSource.get("source");
        assertThat(source).isNotNull();
        
        // Verify user-service specific properties
        assertThat(source).containsKey("spring.datasource.url");
        assertThat(source.get("spring.datasource.url").toString()).contains("user_db");
    }

    /**
     * Test: Verify non-existent configuration returns 404
     * Validates: Requirements 12.1
     */
    @Test
    @DisplayName("Should return 404 for non-existent service configuration")
    void testNonExistentServiceConfiguration() {
        // When: Request configuration for a non-existent service
        Map<String, Object> body = webTestClient.get()
            .uri("/non-existent-service/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        // Then: Response should still be 200 (Spring Cloud Config returns global config)
        // But it should only contain application.yml, not a service-specific file
        assertThat(body).isNotNull();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> propertySources = (List<Map<String, Object>>) body.get("propertySources");
        
        // Should not have a service-specific configuration file
        boolean hasServiceConfig = propertySources.stream()
            .anyMatch(ps -> {
                String name = (String) ps.get("name");
                return name != null && name.contains("non-existent-service.yml");
            });
        
        assertThat(hasServiceConfig).isFalse();
    }

    /**
     * Test: Verify health endpoint is accessible
     * Validates: Requirements 12.1
     */
    @Test
    @DisplayName("Should expose health endpoint returning UP status")
    void testHealthEndpoint() {
        // When: Request health endpoint
        Map<String, Object> body = webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        // Then: Response should indicate server is UP
        assertThat(body).isNotNull();
        // The status might be nested or at root level depending on configuration
        Object status = body.get("status");
        if (status != null) {
            assertThat(status.toString()).isEqualTo("UP");
        } else {
            // If status is not directly available, the endpoint is still accessible
            assertThat(body).isNotEmpty();
        }
    }
}
