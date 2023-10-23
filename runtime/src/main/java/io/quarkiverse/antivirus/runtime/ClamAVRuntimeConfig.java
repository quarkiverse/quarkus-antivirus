package io.quarkiverse.antivirus.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Allows configuring the ClamAV server.
 * <p>
 * Find more info about ClamAV on <a href="https://www.clamav.net/">https://www.clamav.net/</a>.
 */
@ConfigMapping(prefix = "quarkus.antivirus.clamav")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ClamAVRuntimeConfig {
    /**
     * Default ClamAV TCP port.
     */
    String PORT_TCP = "3310";

    /**
     * Property to enable or disable the virus scanner. Useful for developers who don't want to scan files locally.
     */
    @WithDefault("false")
    boolean enabled();

    /**
     * The IP Address of the machine where ClamAV is running.
     */
    @WithDefault("localhost")
    String host();

    /**
     * The Port of the machine where ClamAV is running.
     */
    @WithDefault(PORT_TCP)
    Integer port();

    /**
     * The timeout of how much time to give CLamAV to scan the virus before failing.
     */
    @WithDefault("60000")
    int scanTimeout();

    /**
     * Size in bytes of the chunks of data to stream to the scanner at a time.
     */
    @WithDefault("10240")
    int chunkSize();

    /**
     * The timeout of how much time to give CLamAV to scan the virus before failing.
     */
    @WithDefault("2000")
    int pingTimeout();

}
