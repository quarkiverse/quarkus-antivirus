package io.quarkiverse.antivirus.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Allows configuring any Internet Content Adaptation Protocol (ICAP) RFC-3507 compliant scanner.
 * <p>
 * Find more info about ICAP on <a href="https://www.ietf.org/rfc/rfc3507.txt">RFC-3507</a>.
 */
@ConfigMapping(prefix = "quarkus.antivirus")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface IcapRuntimeConfig {

    /**
     * Default ICAP TCP port.
     */
    String PORT_TCP = "1344";

    /**
     * Default ICAP host.
     */
    String HOST = "localhost";

    /**
     * Property to enable or disable the virus scanner. Useful for developers who don't want to scan files locally.
     */
    @WithName("icap.enabled")
    @WithDefault("false")
    boolean enabled();

    /**
     * The IP Address of the machine where ICAP server is running.
     */
    @WithName("icap.host")
    @WithDefault(HOST)
    String host();

    /**
     * The Port of the machine where ICAP server is running.
     */
    @WithName("icap.port")
    @WithDefault(PORT_TCP)
    Integer port();

    /**
     * The service name of the ICAP service such as "srv_clamav". If not defined this service will not be activated.
     */
    @WithName("icap.service")
    Optional<String> service();

}