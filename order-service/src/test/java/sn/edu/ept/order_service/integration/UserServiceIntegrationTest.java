package sn.edu.ept.order_service.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import sn.edu.ept.order_service.client.UserServiceClient;
import sn.edu.ept.order_service.config.TestConfig;
import sn.edu.ept.order_service.dto.UserExistsResponse;
import sn.edu.ept.order_service.dto.UserResponse;
import sn.edu.ept.order_service.exception.ResourceNotFoundException;
import sn.edu.ept.order_service.exception.ServiceUnavailableException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end integration tests for order-service → user-service communication
 * Uses WireMock to simulate user-service responses
 * 
 * **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6**
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "user-service.url=http://localhost:8082"
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@Import(TestConfig.class)
class UserServiceIntegrationTest {

    @Autowired
    private UserServiceClient userServiceClient;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
            .port(8082)
            .bindAddress("localhost"));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8082);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    void testGetUser_Success() {
        // Given: WireMock stub for successful user retrieval
        stubFor(get(urlEqualTo("/api/users/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "id": 1,
                        "username": "john_doe",
                        "email": "john.doe@example.com",
                        "phone": "+221771234567",
                        "role": "CUSTOMER"
                    }
                    """)));

        // When: Calling getUser
        UserResponse response = userServiceClient.getUser(1L);

        // Then: Response should match expected values
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getPhone()).isEqualTo("+221771234567");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/users/1")));
    }

    @Test
    void testValidateUserExists_Success_UserExists() {
        // Given: WireMock stub for user existence check (user exists)
        stubFor(get(urlEqualTo("/api/users/1/exists"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "exists": true
                    }
                    """)));

        // When: Calling validateUserExists
        UserExistsResponse response = userServiceClient.validateUserExists(1L);

        // Then: Response should indicate user exists
        assertThat(response).isNotNull();
        assertThat(response.isExists()).isTrue();

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/users/1/exists")));
    }

    @Test
    void testValidateUserExists_Success_UserDoesNotExist() {
        // Given: WireMock stub for user existence check (user does not exist)
        stubFor(get(urlEqualTo("/api/users/999/exists"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "exists": false
                    }
                    """)));

        // When: Calling validateUserExists
        UserExistsResponse response = userServiceClient.validateUserExists(999L);

        // Then: Response should indicate user does not exist
        assertThat(response).isNotNull();
        assertThat(response.isExists()).isFalse();

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/users/999/exists")));
    }

    @Test
    void testGetUser_NotFound_ThrowsResourceNotFoundException() {
        // Given: WireMock stub for 404 Not Found
        stubFor(get(urlEqualTo("/api/users/999"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"User not found\"}")));

        // When/Then: Calling getUser should throw ResourceNotFoundException
        assertThatThrownBy(() -> userServiceClient.getUser(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Resource not found");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/users/999")));
    }

    @Test
    void testGetUser_InternalServerError_ThrowsServiceUnavailableException() {
        // Given: WireMock stub for 500 Internal Server Error
        stubFor(get(urlEqualTo("/api/users/2"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Internal server error\"}")));

        // When/Then: Calling getUser should throw ServiceUnavailableException
        assertThatThrownBy(() -> userServiceClient.getUser(2L))
            .isInstanceOf(ServiceUnavailableException.class)
            .hasMessageContaining("Internal server error");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/users/2")));
    }

    @Test
    void testGetUser_ServiceUnavailable_ThrowsServiceUnavailableException() {
        // Given: WireMock stub for 503 Service Unavailable
        stubFor(get(urlEqualTo("/api/users/3"))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Service unavailable\"}")));

        // When/Then: Calling getUser should throw ServiceUnavailableException
        assertThatThrownBy(() -> userServiceClient.getUser(3L))
            .isInstanceOf(ServiceUnavailableException.class)
            .hasMessageContaining("unavailable");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/users/3")));
    }

    @Test
    void testGetUser_Timeout_ThrowsException() {
        // Given: WireMock stub with delay to simulate timeout
        stubFor(get(urlEqualTo("/api/users/4"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 4}")
                .withFixedDelay(6000))); // 6 second delay (more than 5 second timeout)

        // When/Then: Calling getUser should throw exception due to timeout
        assertThatThrownBy(() -> userServiceClient.getUser(4L))
            .hasMessageContaining("timed out");
    }

    @Test
    void testValidateUserExists_NotFound_ThrowsResourceNotFoundException() {
        // Given: WireMock stub for 404 Not Found on exists check
        stubFor(get(urlEqualTo("/api/users/888/exists"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"User not found\"}")));

        // When/Then: Calling validateUserExists should throw ResourceNotFoundException
        assertThatThrownBy(() -> userServiceClient.validateUserExists(888L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Resource not found");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/users/888/exists")));
    }

    @Test
    void testValidateUserExists_InternalServerError_ThrowsServiceUnavailableException() {
        // Given: WireMock stub for 500 Internal Server Error on exists check
        stubFor(get(urlEqualTo("/api/users/5/exists"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Internal server error\"}")));

        // When/Then: Calling validateUserExists should throw ServiceUnavailableException
        assertThatThrownBy(() -> userServiceClient.validateUserExists(5L))
            .isInstanceOf(ServiceUnavailableException.class)
            .hasMessageContaining("Internal server error");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/users/5/exists")));
    }
}
