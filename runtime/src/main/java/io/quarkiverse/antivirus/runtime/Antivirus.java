package io.quarkiverse.antivirus.runtime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import lombok.extern.jbosslog.JBossLog;

/**
 * Antivirus service manager will execute virus scan against all registered engines.
 */
@ApplicationScoped
@JBossLog
public class Antivirus {

    @Inject
    private Instance<AntivirusEngine> engineInstances;

    /**
     * Perform virus scan and throw exception if a virus has been detected.
     *
     * @param filename the name of the file to scan
     * @param inputStream the inputStream containing the file contents
     * @return the List of all {@link AntivirusScanResult} from all engines
     */
    public List<AntivirusScanResult> scan(final String filename, final InputStream inputStream) {
        final List<AntivirusScanResult> results = new ArrayList<>();
        for (AntivirusEngine plugin : engineInstances) {
            if (plugin.isEnabled()) {
                final AntivirusScanResult result = plugin.scan(filename, inputStream);
                results.add(result);
            }
        }

        // let user know nothing happened meaning they had all engines disabled
        if (results.isEmpty()) {
            log.warn("Antivirus extension found NO scanning engines to execute!");
        }
        return results;
    }
}
