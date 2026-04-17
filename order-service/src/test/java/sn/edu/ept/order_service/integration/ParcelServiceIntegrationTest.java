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
import sn.edu.ept.order_service.client.ParcelServiceClient;
import sn.edu.ept.order_service.config.TestConfig;
import sn.edu.ept.order_service.dto.ParcelResponse;
import sn.edu.ept.order_service.dto.TariffResponse;
import sn.edu.ept.order_service.exception.ResourceNotFoundException;
import sn.edu.ept.order_service.exception.ServiceUnavailableException;

import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end integration tests for order-service → parcel-service communication
 * Uses WireMock to simulate parcel-service responses
 * 
 * **Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6**
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "parcel-service.url=http://localhost:8083"
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@Import(TestConfig.class)
class ParcelServiceIntegrationTest {

    @Autowired
    private ParcelServiceClient parcelServiceClient;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
            .port(8083)
            .bindAddress("localhost"));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8083);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    void testGetParcel_Success() {
        // Given: WireMock stub for successful parcel retrieval
        stubFor(get(urlEqualTo("/api/parcels/1"))
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

        // When: Calling getParcel
        ParcelResponse response = parcelServiceClient.getParcel(1L);

        // Then: Response should match expected values
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSenderId()).isEqualTo(100L);
        assertThat(response.getDescription()).isEqualTo("Test parcel");
        assertThat(response.getType()).isEqualTo("STANDARD");
        assertThat(response.getWeight()).isEqualTo(5.0);
        assertThat(response.getVolumeM3()).isEqualTo(0.5);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getOriginCity()).isEqualTo("Dakar");
        assertThat(response.getDestinationCity()).isEqualTo("Thies");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/parcels/1")));
    }

    @Test
    void testCalculateTariff_Success() {
        // Given: WireMock stub for successful tariff calculation
        stubFor(post(urlEqualTo("/api/parcels/1/calculate-tariff"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "parcelId": 1,
                        "tariff": 25.5,
                        "basePrice": 5.0,
                        "weightCost": 10.0,
                        "volumeCost": 5.0,
                        "distanceCost": 5.5
                    }
                    """)));

        // When: Calling calculateTariff
        TariffResponse response = parcelServiceClient.calculateTariff(1L);

        // Then: Response should match expected values
        assertThat(response).isNotNull();
        assertThat(response.getParcelId()).isEqualTo(1L);
        assertThat(response.getTariff()).isEqualTo(25.5);
        assertThat(response.getBasePrice()).isEqualTo(5.0);
        assertThat(response.getWeightCost()).isEqualTo(10.0);
        assertThat(response.getVolumeCost()).isEqualTo(5.0);
        assertThat(response.getDistanceCost()).isEqualTo(5.5);

        // Verify the request was made
        verify(postRequestedFor(urlEqualTo("/api/parcels/1/calculate-tariff")));
    }

    @Test
    void testGetParcel_NotFound_ThrowsResourceNotFoundException() {
        // Given: WireMock stub for 404 Not Found
        stubFor(get(urlEqualTo("/api/parcels/999"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Parcel not found\"}")));

        // When/Then: Calling getParcel should throw ResourceNotFoundException
        assertThatThrownBy(() -> parcelServiceClient.getParcel(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Resource not found");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/parcels/999")));
    }

    @Test
    void testGetParcel_InternalServerError_ThrowsServiceUnavailableException() {
        // Given: WireMock stub for 500 Internal Server Error
        stubFor(get(urlEqualTo("/api/parcels/2"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Internal server error\"}")));

        // When/Then: Calling getParcel should throw ServiceUnavailableException
        assertThatThrownBy(() -> parcelServiceClient.getParcel(2L))
            .isInstanceOf(ServiceUnavailableException.class)
            .hasMessageContaining("Internal server error");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/parcels/2")));
    }

    @Test
    void testGetParcel_ServiceUnavailable_ThrowsServiceUnavailableException() {
        // Given: WireMock stub for 503 Service Unavailable
        stubFor(get(urlEqualTo("/api/parcels/3"))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Service unavailable\"}")));

        // When/Then: Calling getParcel should throw ServiceUnavailableException
        assertThatThrownBy(() -> parcelServiceClient.getParcel(3L))
            .isInstanceOf(ServiceUnavailableException.class)
            .hasMessageContaining("unavailable");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/parcels/3")));
    }

    @Test
    void testGetParcel_Timeout_ThrowsException() {
        // Given: WireMock stub with delay to simulate timeout
        stubFor(get(urlEqualTo("/api/parcels/4"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 4}")
                .withFixedDelay(6000))); // 6 second delay (more than 5 second timeout)

        // When/Then: Calling getParcel should throw exception due to timeout
        assertThatThrownBy(() -> parcelServiceClient.getParcel(4L))
            .hasMessageContaining("timed out");
    }

    @Test
    void testCalculateTariff_NotFound_ThrowsResourceNotFoundException() {
        // Given: WireMock stub for 404 Not Found on tariff calculation
        stubFor(post(urlEqualTo("/api/parcels/999/calculate-tariff"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Parcel not found\"}")));

        // When/Then: Calling calculateTariff should throw ResourceNotFoundException
        assertThatThrownBy(() -> parcelServiceClient.calculateTariff(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Resource not found");

        // Verify the request was made
        verify(postRequestedFor(urlEqualTo("/api/parcels/999/calculate-tariff")));
    }
}
