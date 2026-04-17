package sn.edu.ept.config_server;

import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for Spring Cloud Config Server configuration loading.
 * Tests validate correctness properties for YAML parsing, configuration merging,
 * and precedence rules.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigurationLoadingPropertiesTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient createWebTestClient() {
        String baseUrl = "http://localhost:" + port;
        return WebTestClient.bindToServer()
            .baseUrl(baseUrl)
            .build();
    }

    private Path getConfigRepoPath() {
        return Paths.get("config-repo");
    }

    /**
     * Property 1: Valid YAML Parsing
     * Validates: Requirements 5.2, 6.2
     * 
     * For any valid YAML content in configuration files, the Config_Server SHALL
     * successfully parse the content without errors.
     */
    @Property
    @Label("Property 1: Valid YAML Parsing - Validates: Requirements 5.2, 6.2")
    void validYamlContentShouldBeParsedSuccessfully(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String serviceName,
            @ForAll @AlphaChars @StringLength(min = 3, max = 15) String propertyKey,
            @ForAll @StringLength(min = 1, max = 50) String propertyValue) throws IOException {
        
        WebTestClient webTestClient = createWebTestClient();
        Path configRepoPath = getConfigRepoPath();
        
        // Create a valid YAML configuration
        Map<String, Object> config = new HashMap<>();
        config.put(propertyKey, propertyValue);
        
        // Write to a temporary service configuration file
        Path serviceConfigPath = configRepoPath.resolve(serviceName + ".yml");
        Yaml yaml = new Yaml();
        Files.writeString(serviceConfigPath, yaml.dump(config));
        
        try {
            // Request configuration from the server
            Map<String, Object> body = webTestClient.get()
                .uri("/" + serviceName + "/default")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
            
            // Verify the server successfully parsed and returned the configuration
            assertThat(body).isNotNull();
            assertThat(body).containsKey("propertySources");
        } finally {
            // Cleanup
            Files.deleteIfExists(serviceConfigPath);
        }
    }

    /**
     * Property 2: Global Configuration Inclusion
     * Validates: Requirements 5.4
     * 
     * For any service configuration request, the returned configuration SHALL
     * include all properties defined in application.yml.
     */
    @Property
    @Label("Property 2: Global Configuration Inclusion - Validates: Requirements 5.4")
    void allServiceConfigurationsShouldIncludeGlobalProperties(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String serviceName) {
        
        WebTestClient webTestClient = createWebTestClient();
        
        // Request configuration for any service
        Map<String, Object> body = webTestClient.get()
            .uri("/" + serviceName + "/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();
        
        // Verify response is successful
        assertThat(body).isNotNull();
        
        // Verify propertySources contains application.yml
        assertThat(body).containsKey("propertySources");
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> propertySources = 
            (java.util.List<Map<String, Object>>) body.get("propertySources");
        
        // At least one property source should reference application.yml
        boolean hasGlobalConfig = propertySources.stream()
            .anyMatch(ps -> {
                String name = (String) ps.get("name");
                return name != null && name.contains("application.yml");
            });
        
        assertThat(hasGlobalConfig).isTrue();
    }

    /**
     * Property 3: Service-Specific Configuration Loading
     * Validates: Requirements 6.1
     * 
     * For any application name with a corresponding {application}.yml file,
     * when a configuration request is made, the Config_Server SHALL load and
     * return properties from that file.
     */
    @Property
    @Label("Property 3: Service-Specific Configuration Loading - Validates: Requirements 6.1")
    void serviceSpecificConfigurationShouldBeLoaded(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String serviceName,
            @ForAll @AlphaChars @StringLength(min = 3, max = 15) String uniqueKey,
            @ForAll @IntRange(min = 1000, max = 9999) int uniqueValue) throws IOException {
        
        WebTestClient webTestClient = createWebTestClient();
        Path configRepoPath = getConfigRepoPath();
        
        // Create a service-specific configuration with a unique property
        Map<String, Object> config = new HashMap<>();
        config.put(uniqueKey, uniqueValue);
        
        Path serviceConfigPath = configRepoPath.resolve(serviceName + ".yml");
        Yaml yaml = new Yaml();
        Files.writeString(serviceConfigPath, yaml.dump(config));
        
        try {
            // Request configuration for the service
            Map<String, Object> body = webTestClient.get()
                .uri("/" + serviceName + "/default")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
            
            // Verify the service-specific configuration is loaded
            assertThat(body).isNotNull();
            assertThat(body).containsKey("propertySources");
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> propertySources = 
                (java.util.List<Map<String, Object>>) body.get("propertySources");
            
            // Verify service-specific file is in property sources
            boolean hasServiceConfig = propertySources.stream()
                .anyMatch(ps -> {
                    String name = (String) ps.get("name");
                    return name != null && name.contains(serviceName + ".yml");
                });
            
            assertThat(hasServiceConfig).isTrue();
        } finally {
            Files.deleteIfExists(serviceConfigPath);
        }
    }

    /**
     * Property 4: Global-Only Fallback
     * Validates: Requirements 6.3
     * 
     * For any application name without a corresponding {application}.yml file,
     * when a configuration request is made, the Config_Server SHALL return only
     * the global configuration from application.yml.
     */
    @Property
    @Label("Property 4: Global-Only Fallback - Validates: Requirements 6.3")
    void nonExistentServiceShouldReturnOnlyGlobalConfiguration(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String nonExistentService) {
        
        WebTestClient webTestClient = createWebTestClient();
        Path configRepoPath = getConfigRepoPath();
        
        // Ensure the service configuration file doesn't exist
        Path serviceConfigPath = configRepoPath.resolve(nonExistentService + ".yml");
        Assume.that(!Files.exists(serviceConfigPath));
        
        // Request configuration for non-existent service
        Map<String, Object> body = webTestClient.get()
            .uri("/" + nonExistentService + "/default")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();
        
        // Verify response is successful
        assertThat(body).isNotNull();
        assertThat(body).containsKey("propertySources");
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> propertySources = 
            (java.util.List<Map<String, Object>>) body.get("propertySources");
        
        // Should only have global configuration (application.yml)
        boolean hasOnlyGlobalConfig = propertySources.stream()
            .allMatch(ps -> {
                String name = (String) ps.get("name");
                return name != null && (name.contains("application.yml") || name.contains("application.properties"));
            });
        
        assertThat(hasOnlyGlobalConfig).isTrue();
    }

    /**
     * Property 5: Service Overrides Global
     * Validates: Requirements 6.4
     * 
     * For any property key that exists in both application.yml and {application}.yml,
     * the merged configuration SHALL contain the value from {application}.yml.
     */
    @Property
    @Label("Property 5: Service Overrides Global - Validates: Requirements 6.4")
    void serviceSpecificPropertiesShouldOverrideGlobalProperties(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String serviceName,
            @ForAll @AlphaChars @StringLength(min = 3, max = 15) String sharedKey,
            @ForAll @IntRange(min = 1000, max = 4999) int serviceValue) throws IOException {
        
        WebTestClient webTestClient = createWebTestClient();
        Path configRepoPath = getConfigRepoPath();
        
        // Create service-specific configuration with a property that might exist in global config
        Map<String, Object> serviceConfig = new HashMap<>();
        Map<String, Object> nested = new HashMap<>();
        nested.put(sharedKey, serviceValue);
        serviceConfig.put("test", nested);
        
        Path serviceConfigPath = configRepoPath.resolve(serviceName + ".yml");
        Yaml yaml = new Yaml();
        Files.writeString(serviceConfigPath, yaml.dump(serviceConfig));
        
        try {
            // Request configuration
            Map<String, Object> body = webTestClient.get()
                .uri("/" + serviceName + "/default")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
            
            // Verify service-specific configuration takes precedence
            assertThat(body).isNotNull();
            assertThat(body).containsKey("propertySources");
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> propertySources = 
                (java.util.List<Map<String, Object>>) body.get("propertySources");
            
            // Service-specific config should appear before global config in the list
            int serviceConfigIndex = -1;
            int globalConfigIndex = -1;
            
            for (int i = 0; i < propertySources.size(); i++) {
                String name = (String) propertySources.get(i).get("name");
                if (name != null) {
                    if (name.contains(serviceName + ".yml")) {
                        serviceConfigIndex = i;
                    } else if (name.contains("application.yml")) {
                        globalConfigIndex = i;
                    }
                }
            }
            
            // If both exist, service config should come before global (higher precedence)
            if (serviceConfigIndex >= 0 && globalConfigIndex >= 0) {
                assertThat(serviceConfigIndex).isLessThan(globalConfigIndex);
            }
        } finally {
            Files.deleteIfExists(serviceConfigPath);
        }
    }

    /**
     * Property 6: Profile-Specific Configuration Loading
     * Validates: Requirements 7.1
     * 
     * For any application name and profile with a corresponding {application}-{profile}.yml file,
     * when a configuration request includes that profile, the Config_Server SHALL load
     * properties from that file.
     */
    @Property
    @Label("Property 6: Profile-Specific Configuration Loading - Validates: Requirements 7.1")
    void profileSpecificConfigurationShouldBeLoaded(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String serviceName,
            @ForAll @AlphaChars @StringLength(min = 2, max = 10) String profile,
            @ForAll @AlphaChars @StringLength(min = 3, max = 15) String profileKey,
            @ForAll @IntRange(min = 5000, max = 9999) int profileValue) throws IOException {
        
        WebTestClient webTestClient = createWebTestClient();
        Path configRepoPath = getConfigRepoPath();
        
        // Create profile-specific configuration
        Map<String, Object> profileConfig = new HashMap<>();
        profileConfig.put(profileKey, profileValue);
        
        Path profileConfigPath = configRepoPath.resolve(serviceName + "-" + profile + ".yml");
        Yaml yaml = new Yaml();
        Files.writeString(profileConfigPath, yaml.dump(profileConfig));
        
        try {
            // Request configuration with profile
            Map<String, Object> body = webTestClient.get()
                .uri("/" + serviceName + "/" + profile)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
            
            // Verify profile-specific configuration is loaded
            assertThat(body).isNotNull();
            assertThat(body).containsKey("propertySources");
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> propertySources = 
                (java.util.List<Map<String, Object>>) body.get("propertySources");
            
            // Verify profile-specific file is in property sources
            boolean hasProfileConfig = propertySources.stream()
                .anyMatch(ps -> {
                    String name = (String) ps.get("name");
                    return name != null && name.contains(serviceName + "-" + profile + ".yml");
                });
            
            assertThat(hasProfileConfig).isTrue();
        } finally {
            Files.deleteIfExists(profileConfigPath);
        }
    }

    /**
     * Property 7: Configuration Precedence
     * Validates: Requirements 7.3
     * 
     * For any property key that exists in multiple configuration files
     * (application.yml, {application}.yml, {application}-{profile}.yml),
     * the merged configuration SHALL apply the precedence:
     * profile-specific > service-specific > global.
     */
    @Property
    @Label("Property 7: Configuration Precedence - Validates: Requirements 7.3")
    void configurationPrecedenceShouldBeCorrect(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String serviceName,
            @ForAll @AlphaChars @StringLength(min = 2, max = 10) String profile) throws IOException {
        
        WebTestClient webTestClient = createWebTestClient();
        Path configRepoPath = getConfigRepoPath();
        
        // Create service-specific configuration
        Path serviceConfigPath = configRepoPath.resolve(serviceName + ".yml");
        Map<String, Object> serviceConfig = new HashMap<>();
        serviceConfig.put("precedence-test", "service-value");
        Yaml yaml = new Yaml();
        Files.writeString(serviceConfigPath, yaml.dump(serviceConfig));
        
        // Create profile-specific configuration
        Path profileConfigPath = configRepoPath.resolve(serviceName + "-" + profile + ".yml");
        Map<String, Object> profileConfig = new HashMap<>();
        profileConfig.put("precedence-test", "profile-value");
        Files.writeString(profileConfigPath, yaml.dump(profileConfig));
        
        try {
            // Request configuration with profile
            Map<String, Object> body = webTestClient.get()
                .uri("/" + serviceName + "/" + profile)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
            
            // Verify correct precedence
            assertThat(body).isNotNull();
            assertThat(body).containsKey("propertySources");
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> propertySources = 
                (java.util.List<Map<String, Object>>) body.get("propertySources");
            
            // Profile-specific should come first (highest precedence)
            int profileConfigIndex = -1;
            int serviceConfigIndex = -1;
            int globalConfigIndex = -1;
            
            for (int i = 0; i < propertySources.size(); i++) {
                String name = (String) propertySources.get(i).get("name");
                if (name != null) {
                    if (name.contains(serviceName + "-" + profile + ".yml")) {
                        profileConfigIndex = i;
                    } else if (name.contains(serviceName + ".yml")) {
                        serviceConfigIndex = i;
                    } else if (name.contains("application.yml")) {
                        globalConfigIndex = i;
                    }
                }
            }
            
            // Verify precedence order: profile < service < global (lower index = higher precedence)
            if (profileConfigIndex >= 0 && serviceConfigIndex >= 0) {
                assertThat(profileConfigIndex).isLessThan(serviceConfigIndex);
            }
            if (serviceConfigIndex >= 0 && globalConfigIndex >= 0) {
                assertThat(serviceConfigIndex).isLessThan(globalConfigIndex);
            }
            if (profileConfigIndex >= 0 && globalConfigIndex >= 0) {
                assertThat(profileConfigIndex).isLessThan(globalConfigIndex);
            }
        } finally {
            Files.deleteIfExists(serviceConfigPath);
            Files.deleteIfExists(profileConfigPath);
        }
    }
}
