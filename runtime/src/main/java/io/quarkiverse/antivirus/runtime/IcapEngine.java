package io.quarkiverse.antivirus.runtime;

import java.io.IOException;
import java.io.InputStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.github.toolarium.icap.client.ICAPClientFactory;
import com.github.toolarium.icap.client.dto.ICAPMode;
import com.github.toolarium.icap.client.dto.ICAPRequestInformation;
import com.github.toolarium.icap.client.dto.ICAPResource;
import com.github.toolarium.icap.client.exception.ContentBlockedException;

import lombok.extern.jbosslog.JBossLog;

/**
 * {@link AntivirusEngine} implementation for Internet Content Adaptation Protocol (ICAP) RFC-3507 compliant scanner.
 * <p>
 * Find more info about ICAP on <a href="https://www.ietf.org/rfc/rfc3507.txt">RFC-3507</a>.
 */
@ApplicationScoped
@JBossLog
public class IcapEngine implements AntivirusEngine {

    @Inject
    IcapRuntimeConfig config;

    @Override
    public boolean isEnabled() {
        return config.enabled() && config.service().isPresent();
    }

    /**
     * Scans an {@link InputStream} for any viruses. If any virus is detected an exception will be thrown.
     *
     * @param filename the name of the file to be scanned
     * @param inputStream the {@link InputStream} of the file to scan
     * @return the {@link AntivirusScanResult} containing the results
     */
    @Override
    public AntivirusScanResult scan(String filename, InputStream inputStream) {
        AntivirusScanResult.AntivirusScanResultBuilder result = AntivirusScanResult.builder().engine("ICAP")
                .fileName(filename);
        if (!config.enabled()) {
            return result.status(404).message("Internet Content Adaptation Protocol scanner is currently disabled!").build();
        }

        try {
            // the user, request source and the resource
            final String username = "Quarkus Antivirus";
            final String requestSource = "file";

            // reset the input stream after calculating length
            final long resourceLength = inputStream.readAllBytes().length;
            inputStream.reset();

            // connect and validate with ICAP server
            ICAPClientFactory.getInstance().getICAPClient(config.host(), config.port(), config.service().orElseThrow())
                    .validateResource(ICAPMode.REQMOD, new ICAPRequestInformation(username, requestSource),
                            new ICAPResource(filename, inputStream, resourceLength));

            // If no exception is thrown the resource can be used and is valid.
            return result.status(200).build();
        } catch (IOException ioe) {
            log.warnf("Resource could not be accessed: %s", ioe.getMessage(), ioe);
            return result.status(400).message("Cannot perform virus scan").payload(ioe.getMessage()).build();
        } catch (ContentBlockedException e) { // !!! The resource has to be blocked !!!
            // The e.getContent() contains the returned error information from the ICAP-Server.
            // It can be ignored as long as the resource is blocked; otherwise it gives a well-structured response.
            return result.status(400).message(e.getMessage()).payload(e.getContent()).build();
        }
    }
}