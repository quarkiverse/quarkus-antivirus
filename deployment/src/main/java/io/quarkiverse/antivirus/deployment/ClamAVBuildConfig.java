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
@ConfigMapping(prefix = "quarkus.antivirus.clamav")
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
    @WithName("devservice.enabled")
    @WithDefault("false")
    boolean enabled();

    /**
     * The ClamAV container image to use.
     */
    @WithName("devservice.image-name")
    @WithDefault(DEFAULT_IMAGE)
    String imageName();

    /**
     * The ClamAV container image to use.
     */
    @WithName("devservice.startup-timeout")
    @WithDefault("1800")
    Integer startupTimeout();

    /**
     * Flag to enable the FreshClam daemon to update the virus database daily. Default it is disabled.
     */
    @WithName("devservice.fresh-clam")
    @WithDefault("false")
    boolean freshClam();

    /**
     * If ClamAv registers in the health check by pinging the service.
     */
    @WithName("health.enabled")
    @WithDefault("true")
    boolean healthEnabled();

}