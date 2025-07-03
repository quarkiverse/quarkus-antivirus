package io.quarkiverse.antivirus.deployment;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.devservices.common.ContainerShutdownCloseable;

/**
 * Starts a ClamAV server as dev service if needed.
 */
@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = DevServicesConfig.Enabled.class)
public class ClamAVDevServicesProcessor {

    private static final Logger log = Logger.getLogger(ClamAVDevServicesProcessor.class);

    /**
     * Label to add to shared Dev Service for ClamAV running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-clamav";

    static final ContainerLocator containerLocator = new ContainerLocator(DEV_SERVICE_LABEL, ClamAVContainer.PORT_TCP);

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
            DevServicesConfig devServicesConfig,
            BuildProducer<ClamAVDevServicesConfigBuildItem> clamAvBuildItemBuildProducer,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem) {

        if (devService != null) {
            boolean shouldShutdownTheBroker = !ClamAVBuildConfig.isEqual(clamAVConfig, cfg);
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
            devService = startClamAV(dockerStatusBuildItem, clamAVConfig, devServicesConfig, launchMode,
                    !devServicesSharedNetworkBuildItem.isEmpty());
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
            DevServicesConfig devServicesConfig,
            LaunchModeBuildItem launchMode,
            boolean useSharedNetwork) {
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

        if (ClamAVContainer.getTcpHost().isPresent()) {
            // no mailer configured
            log.warn("Not starting dev services for ClamAV, as 'quarkus.antivirus.clamav.host' has been configured.");
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            log.warn("Docker isn't working, not starting dev services for ClamAV.");
            return null;
        }

        final Optional<ContainerAddress> maybeContainerAddress = containerLocator.locateContainer(clamAVConfig.serviceName(),
                useSharedNetwork,
                launchMode.getLaunchMode());

        final Supplier<DevServicesResultBuildItem.RunningDevService> defaultClamAvSupplier = () -> {
            final ClamAVContainer container = new ClamAVContainer(clamAVConfig, useSharedNetwork);
            devServicesConfig.timeout().ifPresent(container::withStartupTimeout);
            container.start();

            return new DevServicesResultBuildItem.RunningDevService(clamAVConfig.serviceName(),
                    container.getContainerId(),
                    new ContainerShutdownCloseable(container, clamAVConfig.serviceName()),
                    container.getExposedConfig());
        };

        return maybeContainerAddress
                .map(containerAddress -> new DevServicesResultBuildItem.RunningDevService(clamAVConfig.serviceName(),
                        containerAddress.getId(),
                        null,
                        new HashMap<>()))
                .orElseGet(defaultClamAvSupplier);
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
