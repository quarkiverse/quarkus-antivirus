package io.quarkiverse.antivirus.runtime;

import java.io.InputStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * Antivirus service manager will execute virus scan against all registered engines.
 */
@ApplicationScoped
public class Antivirus {

    @Inject
    private Instance<AntivirusEngine> engineInstances;

    /**
     * Perform virus scan and throw exception if a virus has been detected.
     *
     * @param filename the name of the file to scan
     * @param inputStream the inputStream containing the file contents
     */
    public void scan(final String filename, final InputStream inputStream) {
        for (AntivirusEngine plugin : engineInstances) {
            if (plugin.isEnabled()) {
                plugin.scan(filename, inputStream);
            }
        }
    }
}