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
    @WithDefault("false")
    boolean enabled();

    /**
     * The ClamAV container image to use.
     */
    @WithName("clamav.devservice.image-name")
    @WithDefault(DEFAULT_IMAGE)
    String imageName();

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