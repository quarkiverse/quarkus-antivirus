package io.quarkiverse.antivirus.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Allows configuring the VirusTotal server.
 * <p>
 * Find more info about VirusTotal on <a href="https://developers.virustotal.com/reference">VirusTotal Public API v3.0</a>.
 */
@ConfigMapping(prefix = "quarkus.antivirus")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface VirusTotalRuntimeConfig {
    /**
     * Default VirusTotal API Endppoint
     */
    String ENDPOINT = "https://www.virustotal.com/api/v3/files/%s";

    /**
     * Property to enable or disable the virus scanner. Useful for developers who don't want to scan files locally.
     */
    @WithName("virustotal.enabled")
    @WithDefault("false")
    boolean enabled();

    /**
     * The API endpoint for VirusTotal.
     */
    @WithName("virustotal.url")
    @WithDefault(ENDPOINT)
    String url();

    /**
     * The API key for VirusTotal.
     */
    @WithName("virustotal.key")
    Optional<String> key();

    /**
     * VirusTotal checks over 70+ different engine for virus and collates a count of how many of those 70 reported a file as
     * malicious. This number lets you control how many engines have to report a file is malicious to raise an exception.
     */
    @WithName("virustotal.minimum-votes")
    @WithDefault("1")
    Integer minimumVotes();
}