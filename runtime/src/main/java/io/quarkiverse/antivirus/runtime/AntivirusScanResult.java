package io.quarkiverse.antivirus.runtime;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jbosslog.JBossLog;

/**
 * Provide the results of a single antivirus engine scan.
 */
@RegisterForReflection
@Data
@AllArgsConstructor
@Builder
@JBossLog
public class AntivirusScanResult {

    /**
     * Status code mirrors HTTP status codes for signaling what occurred so the downstream system can act. 200 = OK.
     */
    private int status;

    /**
     * Scan engine that produced this result.
     */
    private String engine;

    /**
     * Name of the file that was scanned.
     */
    private String fileName;

    /**
     * Human-readable message explaining what happened.
     */
    private String message;

    /**
     * Payload from the engine, for example if its an HTTP service it might be the JSON response raw.
     */
    private String payload;

}