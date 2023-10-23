package io.quarkiverse.antivirus.deployment;

import java.util.Objects;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;

/**
 * Starts a ClamAV server as dev service if needed.
 */
@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
public class ClamAVDevServicesProcessor {

    private static final Logger log = Logger.getLogger(ClamAVDevServicesProcessor.class);

    /**
     * Label to add to shared Dev Service for ClamAV running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-clamav";

    static volatile DevServicesResultBuildItem.RunningDevService devService;
    static volatile ClamAVBuildConfig cfg;
    static volatile boolean first = true;

    @BuildStep
    public DevServicesResultBuildItem startClamAVDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            ClamAVBuildConfig clamAVConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            GlobalDevServicesConfig devServicesConfig,
            BuildProducer<ClamAVDevServicesConfigBuildItem> clamAvBuildItemBuildProducer) {

        if (devService != null) {
            boolean shouldShutdownTheBroker = !clamAVConfig.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            shutdown();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "ClamAV Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startClamAV(dockerStatusBuildItem, clamAVConfig, devServicesConfig);
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        if (devService.isOwner()) {
            log.info("Dev Services for ClamAV started.");
            clamAvBuildItemBuildProducer.produce(new ClamAVDevServicesConfigBuildItem(devService.getConfig()));
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    shutdown();

                    log.info("Dev Services for ClamAV shut down.");
                }
                first = true;
                devService = null;
                cfg = null;
            };
            QuarkusClassLoader cl = (QuarkusClassLoader) Thread.currentThread().getContextClassLoader();
            ((QuarkusClassLoader) cl.parent()).addCloseTask(closeTask);
        }
        cfg = clamAVConfig;
        return devService.toBuildItem();
    }

    private DevServicesResultBuildItem.RunningDevService startClamAV(DockerStatusBuildItem dockerStatusBuildItem,
            ClamAVBuildConfig clamAVConfig,
            GlobalDevServicesConfig devServicesConfig) {
        if (!clamAVConfig.enabled()) {
            // explicitly disabled
            log.warn("Not starting dev services for ClamAV, as it has been disabled in the config.");
            return null;
        }

        if (!Objects.equals(ClamAVContainer.getPort(), ClamAVContainer.PORT_TCP)) {
            // no mailer configured
            log.warn("Not starting dev services for ClamAV, as 'quarkus.antivirus.clamav.port' has been configured.");
            return null;
        }

        if (!ClamAVContainer.getTcpHost().equalsIgnoreCase("localhost")) {
            // no mailer configured
            log.warn("Not starting dev services for ClamAV, as 'quarkus.antivirus.clamav.host' has been configured.");
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            log.warn("Docker isn't working, not starting dev services for ClamAV.");
            return null;
        }

        final ClamAVContainer clamAV = new ClamAVContainer(clamAVConfig);
        devServicesConfig.timeout.ifPresent(clamAV::withStartupTimeout);
        clamAV.start();

        return new DevServicesResultBuildItem.RunningDevService(ClamAVContainer.NAME,
                clamAV.getContainerId(),
                clamAV::close,
                clamAV.getExposedConfig());
    }

    private void shutdown() {
        if (devService != null) {
            try {
                log.info("Dev Services for ClamAV shutting down...");
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the ClamAV server", e);
            } finally {
                devService = null;
            }
        }
    }
}