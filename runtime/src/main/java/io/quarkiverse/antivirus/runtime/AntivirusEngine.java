package io.quarkiverse.antivirus.runtime;

import java.io.InputStream;

/**
 * Service provider interface for virus scanning that might be used in file upload component for example when dealing with
 * untrusted files.
 */
public interface AntivirusEngine {

    /**
     * Indicate whether this {@link AntivirusEngine} is enabled or not.
     *
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    boolean isEnabled();

    /**
     * Perform virus scan and throw exception if a virus has been detected.
     *
     * @param filename the name of the file to scan
     * @param inputStream the inputStream containing the file contents
     * @return the {@link AntivirusScanResult} containing the results
     */
    AntivirusScanResult scan(final String filename, final InputStream inputStream);

}