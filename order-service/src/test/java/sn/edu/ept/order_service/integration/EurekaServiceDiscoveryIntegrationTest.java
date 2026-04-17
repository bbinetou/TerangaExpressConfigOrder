package sn.edu.ept.order_service.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import sn.edu.ept.order_service.client.ParcelServiceClient;
import sn.edu.ept.order_service.client.UserServiceClient;
import sn.edu.ept.order_service.config.TestConfig;
import sn.edu.ept.order_service.dto.ParcelResponse;
import sn.edu.ept.order_service.dto.UserResponse;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end integration tests for Eureka service discovery
 * Tests that Feign clients can resolve services via Eureka
 * 
 * Note: This test runs with Eureka disabled and uses direct URL configuration
 * to simulate service discovery behavior. In a real environment with Eureka enabled,
 * the Feign clients would automatically resolve service locations.
 * 
 * **Validates: Requirements 4.4, 5.4, 6.1, 6.2, 6.3, 6.4, 6.5**
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "parcel-service.url=http://localhost:8083",
    "user-service.url=http://localhost:8082"
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false",
    "parcel-service.ribbon.listOfServers=localhost:8083",
    "user-service.ribbon.listOfServers=localhost:8082"
})
@Import(TestConfig.class)
class EurekaServiceDiscoveryIntegrationTest {

    @Autowired
    private ParcelServiceClient parcelServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired(required = false)
    private DiscoveryClient discoveryClient;

    private WireMockServer parcelServiceMock;
    private WireMockServer userServiceMock;

    @BeforeEach
    void setUp() {
        // Start WireMock servers for parcel-service and user-service
        parcelServiceMock = new WireMockServer(WireMockConfiguration.options()
            .port(8083)
            .bindAddress("localhost"));
        parcelServiceMock.start();
        WireMock.configureFor("localhost", 8083);

        userServiceMock = new WireMockServer(WireMockConfiguration.options()
            .port(8082)
            .bindAddress("localhost"));
        userServiceMock.start();
    }

    @AfterEach
    void tearDown() {
        if (parcelServiceMock != null && parcelServiceMock.isRunning()) {
            parcelServiceMock.stop();
        }
        if (userServiceMock != null && userServiceMock.isRunning()) {
            userServiceMock.stop();
        }
    }

    @Test
    void testFeignClientResolvesParcelServiceViaConfiguration() {
        // Given: WireMock stub for parcel-service
        parcelServiceMock.stubFor(get(urlEqualTo("/api/parcels/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "id": 1,
                        "senderId": 100,
                        "description": "Test parcel",
                        "type": "STANDARD",
                        "weight": 5.0,
                        "volumeM3": 0.5,
                        "status": "PENDING",
                        "originCity": "Dakar",
                        "destinationCity": "Thies",
                        "createdAt": "2025-01-15T10:00:00"
                    }
                    """)));

        // When: Calling parcelServiceClient
        ParcelResponse response = parcelServiceClient.getParcel(1L);

        // Then: Response should be successful, indicating service was resolved
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDescription()).isEqualTo("Test parcel");

        // Verify the request was made to the correct service
        parcelServiceMock.verify(getRequestedFor(urlEqualTo("/api/parcels/1")));
    }

    @Test
    void testFeignClientResolvesUserServiceViaConfiguration() {
        // Given: WireMock stub for user-service
        WireMock.configureFor("localhost", 8082);
        userServiceMock.stubFor(get(urlEqualTo("/api/users/1"))
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

        // When: Calling userServiceClient
        UserResponse response = userServiceClient.getUser(1L);

        // Then: Response should be successful, indicating service was resolved
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("john_doe");

        // Verify the request was made to the correct service
        userServiceMock.verify(getRequestedFor(urlEqualTo("/api/users/1")));
    }

    @Test
    void testServiceUnavailable_WhenServiceNotRunning() {
        // Given: Stop the parcel service mock to simulate service unavailable
        parcelServiceMock.stop();

        // When/Then: Calling parcelServiceClient should throw FeignException
        assertThatThrownBy(() -> parcelServiceClient.getParcel(1L))
            .isInstanceOf(FeignException.class);
    }

    @Test
    void testMultipleServicesCanBeResolvedSimultaneously() {
        // Given: WireMock stubs for both services
        WireMock.configureFor("localhost", 8083);
        parcelServiceMock.stubFor(get(urlEqualTo("/api/parcels/2"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "id": 2,
                        "senderId": 200,
                        "description": "Another parcel",
                        "type": "EXPRESS",
                        "weight": 3.0,
                        "volumeM3": 0.3,
                        "status": "PENDING",
                        "originCity": "Dakar",
                        "destinationCity": "Saint-Louis",
                        "createdAt": "2025-01-15T11:00:00"
                    }
                    """)));

        WireMock.configureFor("localhost", 8082);
        userServiceMock.stubFor(get(urlEqualTo("/api/users/2"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "id": 2,
                        "username": "jane_smith",
                        "email": "jane.smith@example.com",
                        "phone": "+221779876543",
                        "role": "CUSTOMER"
                    }
                    """)));

        // When: Calling both clients
        ParcelResponse parcelResponse = parcelServiceClient.getParcel(2L);
        UserResponse userResponse = userServiceClient.getUser(2L);

        // Then: Both responses should be successful
        assertThat(parcelResponse).isNotNull();
        assertThat(parcelResponse.getId()).isEqualTo(2L);
        assertThat(parcelResponse.getDescription()).isEqualTo("Another parcel");

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(2L);
        assertThat(userResponse.getUsername()).isEqualTo("jane_smith");

        // Verify both requests were made
        parcelServiceMock.verify(getRequestedFor(urlEqualTo("/api/parcels/2")));
        userServiceMock.verify(getRequestedFor(urlEqualTo("/api/users/2")));
    }

    @Test
    void testEurekaClientDisabledInTestProfile() {
        // When: Checking if DiscoveryClient is available
        // Then: DiscoveryClient should be null or not functional in test profile
        // This verifies that eureka.client.enabled=false is working
        if (discoveryClient != null) {
            // If DiscoveryClient exists, it should return empty lists
            List<String> services = discoveryClient.getServices();
            assertThat(services).isEmpty();
        }
    }

    @Test
    void testFeignClientUsesConfiguredTimeouts() {
        // Given: WireMock stub with a delay less than configured timeout
        WireMock.configureFor("localhost", 8083);
        parcelServiceMock.stubFor(get(urlEqualTo("/api/parcels/3"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "id": 3,
                        "senderId": 300,
                        "description": "Delayed parcel",
                        "type": "STANDARD",
                        "weight": 2.0,
                        "volumeM3": 0.2,
                        "status": "PENDING",
                        "originCity": "Dakar",
                        "destinationCity": "Kaolack",
                        "createdAt": "2025-01-15T12:00:00"
                    }
                    """)
                .withFixedDelay(2000))); // 2 second delay (less than 5 second timeout)

        // When: Calling parcelServiceClient
        ParcelResponse response = parcelServiceClient.getParcel(3L);

        // Then: Response should be successful (within timeout)
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(3L);

        // Verify the request was made
        parcelServiceMock.verify(getRequestedFor(urlEqualTo("/api/parcels/3")));
    }
}
