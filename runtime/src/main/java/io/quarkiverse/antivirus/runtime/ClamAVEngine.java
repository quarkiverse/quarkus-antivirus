package io.quarkiverse.antivirus.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import lombok.extern.jbosslog.JBossLog;

/**
 * {@link AntivirusEngine} implementation for ClamAV Daemon.
 * <p>
 * Find more info about ClamAV on <a href="https://www.clamav.net/">https://www.clamav.net/</a>.
 */
@ApplicationScoped
@JBossLog
public class ClamAVEngine implements AntivirusEngine {
    private ClamAVClient client;
    @Inject
    ClamAVRuntimeConfig config;

    @Override
    public boolean isEnabled() {
        return config.enabled();
    }

    /**
     * Scans an {@link InputStream} for any viruses. If any virus is detected an exception will be thrown.
     *
     * @param filename the name of the file to be scanned
     * @param inputStream the {@link InputStream} of the file to scan
     * @return the {@link AntivirusScanResult} containing the results
     */
    @Override
    public AntivirusScanResult scan(final String filename, final InputStream inputStream) {
        AntivirusScanResult.AntivirusScanResultBuilder result = AntivirusScanResult.builder().engine("ClamAV")
                .fileName(filename);
        if (!config.enabled()) {
            return result.status(404).message("ClamAV scanner is currently disabled!").build();
        }
        log.infof("Starting the virus scan for file: %s", filename);

        try {
            final ClamAVClient client = getClamAVClient();
            final byte[] reply = client.scan(inputStream);
            final String message = new String(reply, StandardCharsets.US_ASCII).trim();
            log.infof("Scanner replied with message: %s", message);
            if (!ClamAVClient.isCleanReply(reply)) {
                final String error = String.format("Scan detected viruses in file '%s'! Virus scanner message = %s",
                        filename, message);
                log.errorf("ClamAV %s", error);
                return result.status(400).message(error).payload(message).build();
            }
            return result.status(200).message(message).payload(message).build();
        } catch (final RuntimeException | IOException ex) {
            final String error = String.format("Unexpected error scanning file '%s' - %s",
                    filename, ex.getMessage());
            return result.status(400).message(error).payload(ex.getMessage()).build();
        } finally {
            log.infof("Finished scanning file %s!", filename);
        }
    }

    /**
     * Ping the clamd service to make sure it is available.
     *
     * @return true if clamd is available false if not.
     */
    public boolean ping() {
        if (!config.enabled()) {
            return false;
        }
        try {
            return getClamAVClient().ping();
        } catch (final RuntimeException | IOException ex) {
            return false;
        }
    }

    /**
     * Returns a new ClamAvClient which can be overridden in unit tests.
     *
     * @return the {@link ClamAVClient}
     */
    ClamAVClient getClamAVClient() {
        if (client != null) {
            return client;
        }
        client = new ClamAVClient(this.config);
        return client;
    }
}
