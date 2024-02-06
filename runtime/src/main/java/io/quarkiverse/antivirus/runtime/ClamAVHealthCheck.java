package io.quarkiverse.antivirus.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness health check to make sure the Clam AV Daemon is available.
 */
@Readiness
@ApplicationScoped
public class ClamAVHealthCheck implements HealthCheck {

    @Inject
    ClamAVEngine engine;

    @Inject
    ClamAVRuntimeConfig config;

    @Override
    public HealthCheckResponse call() {
        final String host = config.host().orElseThrow();
        final String server = String.format("%s:%s", host, config.port());

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("ClamAV Daemon");

        responseBuilder = engine.ping()
                ? responseBuilder.up().withData(server, "UP")
                : responseBuilder.down().withData(server, "DOWN");

        return responseBuilder.build();
    }
}