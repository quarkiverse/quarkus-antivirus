package io.quarkiverse.antivirus.deployment;

import static io.quarkus.devservices.common.ConfigureUtil.configureSharedServiceLabel;
import static io.quarkus.devservices.common.ContainerLocator.locateContainerWithLabels;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jboss.logging.Logger;

import io.quarkus.deployment.IsDevServicesSupportedByLaunchMode;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.Startable;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;

/**
 * Starts a ClamAV server as a dev service if needed.
 */
@BuildSteps(onlyIf = { IsDevServicesSupportedByLaunchMode.class, DevServicesConfig.Enabled.class })
public class ClamAVDevServicesProcessor {

    private static final Logger log = Logger.getLogger(ClamAVDevServicesProcessor.class);

    private static final String FEATURE = "antivirus";

    private static final String CLAMAV_HOST = "quarkus.antivirus.clamav.host";

    private static final String CLAMAV_PORT = "quarkus.antivirus.clamav.port";

    /**
     * Label to add to shared Dev Service for ClamAV running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-clamav";

    private static final ContainerLocator containerLocator = locateContainerWithLabels(ClamAVContainer.PORT_TCP,
            DEV_SERVICE_LABEL);

    @BuildStep
    public DevServicesResultBuildItem startClamAVDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            ClamAVBuildConfig clamAVConfig,
            DevServicesConfig devServicesConfig,
            List<DevServicesSharedNetworkBuildItem> sharedNetwork) {
        if (devServiceDisabled(dockerStatusBuildItem, clamAVConfig)) {
            return null;
        }

        boolean useSharedNetwork = DevServicesSharedNetworkBuildItem.isSharedNetworkRequired(devServicesConfig,
                sharedNetwork);

        return containerLocator
                .locateContainer(clamAVConfig.serviceName(), clamAVConfig.shared(), launchMode.getLaunchMode())
                .map(containerAddress -> DevServicesResultBuildItem.discovered()
                        .name(FEATURE)
                        .containerId(containerAddress.getId())
                        .config(discoveredConfig(containerAddress, clamAVConfig))
                        .build())
                .orElseGet(() -> DevServicesResultBuildItem.owned()
                        .feature(FEATURE)
                        .serviceName(clamAVConfig.serviceName())
                        .serviceConfig(clamAVConfig)
                        .startable(() -> createContainer(clamAVConfig, devServicesConfig, useSharedNetwork, launchMode))
                        .configProvider(configProvider(clamAVConfig))
                        .postStartHook(ClamAVDevServicesProcessor::logStarted)
                        .build());
    }

    private static ClamAVContainer createContainer(ClamAVBuildConfig config, DevServicesConfig devServicesConfig,
            boolean useSharedNetwork, LaunchModeBuildItem launchMode) {
        ClamAVContainer container = new ClamAVContainer(config, useSharedNetwork);
        devServicesConfig.timeout().ifPresent(container::withStartupTimeout);
        configureSharedServiceLabel(container, launchMode.getLaunchMode(), DEV_SERVICE_LABEL, config.serviceName());
        return container;
    }

    private static Map<String, Function<ClamAVContainer, String>> configProvider(ClamAVBuildConfig config) {
        String prefix = config.serviceName();
        return Map.of(
                CLAMAV_HOST, ClamAVContainer::getEffectiveHost,
                CLAMAV_PORT, container -> Integer.toString(container.getEffectivePort()),
                prefix + ".tcp.host", ClamAVContainer::getEffectiveHost,
                prefix + ".tcp.port", container -> Integer.toString(container.getEffectivePort()));
    }

    private static Map<String, String> discoveredConfig(ContainerAddress address, ClamAVBuildConfig config) {
        String prefix = config.serviceName();
        return Map.of(
                CLAMAV_HOST, address.getHost(),
                CLAMAV_PORT, Integer.toString(address.getPort()),
                prefix + ".tcp.host", address.getHost(),
                prefix + ".tcp.port", Integer.toString(address.getPort()));
    }

    private static void logStarted(Startable startable) {
        log.infof(
                "Dev Services for ClamAV started. Other Quarkus applications in dev mode will find the "
                        + "server automatically. For Quarkus applications in production mode, connect using "
                        + "-D%s=%s -D%s=%s",
                CLAMAV_HOST, asClamAVContainer(startable).getEffectiveHost(),
                CLAMAV_PORT, asClamAVContainer(startable).getEffectivePort());
    }

    private static ClamAVContainer asClamAVContainer(Startable startable) {
        return (ClamAVContainer) startable;
    }

    private boolean devServiceDisabled(DockerStatusBuildItem dockerStatusBuildItem, ClamAVBuildConfig config) {
        if (!config.enabled()) {
            log.warn("Not starting dev services for ClamAV, as it has been disabled in the config.");
            return true;
        }

        if (ConfigUtils.isPropertyNonEmpty(CLAMAV_PORT)) {
            log.warn("Not starting dev services for ClamAV, as 'quarkus.antivirus.clamav.port' has been configured.");
            return true;
        }

        if (ConfigUtils.isPropertyNonEmpty(CLAMAV_HOST)) {
            log.warn("Not starting dev services for ClamAV, as 'quarkus.antivirus.clamav.host' has been configured.");
            return true;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            log.warn("Docker isn't working, not starting dev services for ClamAV.");
            return true;
        }

        return false;
    }
}
