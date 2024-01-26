package io.quarkiverse.antivirus.deployment;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Allows configuring the ClamAV server.
 * <p>
 * Find more info about ClamAV on <a href="https://www.clamav.net/">https://www.clamav.net/</a>.
 */
@ConfigMapping(prefix = "quarkus.antivirus")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ClamAVBuildConfig {

    /**
     * Default docker image name.
     */
    String DEFAULT_IMAGE = "clamav/clamav";

    /**
     * Default ClamAV TCP port.
     */
    String PORT_TCP = "3310";

    /**
     * If Dev Services for ClamAV has been explicitly enabled or disabled. Dev Services are generally enabled
     * by default, unless there is an existing configuration present.
     */
    @WithName("clamav.devservice.enabled")
    @WithDefault("true")
    boolean enabled();

    /**
     * The ClamAV container image to use.
     */
    @WithName("clamav.devservice.image-name")
    @WithDefault(DEFAULT_IMAGE)
    String imageName();

    /**
     * Indicates if the ClamAV server managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for ClamAV starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-clamav} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @WithName("clamav.devservice.shared")
    @WithDefault("true")
    boolean shared();

    /**
     * The value of the {@code quarkus-dev-service-clamav} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for Minio looks for a container with the
     * {@code quarkus-dev-service-clamav} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise, it
     * starts a new container with the {@code quarkus-dev-service-clamav} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared ClamAV servers.
     */
    @WithName("clamav.devservice.service-name")
    @WithDefault("clamav")
    String serviceName();

    /**
     * The ClamAV container image to use.
     */
    @WithName("clamav.devservice.startup-timeout")
    @WithDefault("1800")
    Integer startupTimeout();

    /**
     * Flag to enable the FreshClam daemon to update the virus database daily. Default it is disabled.
     */
    @WithName("clamav.devservice.fresh-clam")
    @WithDefault("false")
    boolean freshClam();

    /**
     * Enable or disable ClamAV container logging
     */
    @WithName("clamav.devservice.logging")
    @WithDefault("true")
    boolean logging();

    /**
     * If ClamAv registers in the health check by pinging the service.
     */
    @WithName("clamav.health.enabled")
    @WithDefault("true")
    boolean healthEnabled();

}