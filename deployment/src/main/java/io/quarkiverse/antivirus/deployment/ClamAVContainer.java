package io.quarkiverse.antivirus.deployment;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.quarkus.devservices.common.ConfigureUtil;

/**
 * Testcontainers implementation for ClamAV antivirus server.
 * <p>
 * Supported image: {@code clamav/clamav}
 * Find more info about ClamAV on <a href="https://www.clamav.net/">https://www.clamav.net/</a>.
 * <p>
 * Exposed ports: 3310 (tcp)
 */
public final class ClamAVContainer extends GenericContainer<ClamAVContainer> {

    /**
     * Logger which will be used to capture container STDOUT and STDERR.
     */
    private static final Logger log = Logger.getLogger(ClamAVContainer.class);

    /**
     * Default ClamAV TCP port.
     */
    public static final Integer PORT_TCP = Integer.parseInt(ClamAVBuildConfig.PORT_TCP);

    private final boolean useSharedNetwork;
    private String hostName = null;
    private final ClamAVBuildConfig config;

    ClamAVContainer(ClamAVBuildConfig config, boolean useSharedNetwork) {
        super(DockerImageName.parse(config.imageName()).asCompatibleSubstituteFor(ClamAVBuildConfig.DEFAULT_IMAGE));
        this.useSharedNetwork = useSharedNetwork;
        this.config = config;
        super.withLabel(ClamAVDevServicesProcessor.DEV_SERVICE_LABEL, config.serviceName());
        super.withNetwork(Network.SHARED);
        super.waitingFor(Wait.forLogMessage(".*socket found, clamd started.*", 1));
        super.withStartupTimeout(Duration.ofSeconds(config.testcontainersStartupTimeout()));
        super.withEnv("CLAMD_STARTUP_TIMEOUT", Integer.toString(config.startupTimeout()));
        super.withEnv("CLAMAV_NO_FRESHCLAMD", Boolean.toString(!config.freshClam()));
        super.withEnv("CLAMAV_NO_CLAMD ", "false");
        super.withEnv("CLAMAV_NO_MILTERD", "true");
        super.withCopyFileToContainer(
                MountableFile.forClasspathResource("/clamd.conf"),
                "/etc/clamav/clamd.conf");

        if (useSharedNetwork) {
            super.withReuse(true);
        }

        // forward the container logs
        if (config.logging()) {
            super.withLogConsumer(new JbossContainerLogConsumer(log).withPrefix(config.serviceName()));
        }
    }

    @Override
    protected void configure() {
        super.configure();

        if (useSharedNetwork) {
            hostName = ConfigureUtil.configureSharedNetwork(this, this.config.serviceName());
        } else {
            withNetwork(Network.SHARED);
        }

        addExposedPort(PORT_TCP);
    }

    /**
     * Info about the DevService used in the DevUI.
     *
     * @return the map of as running configuration of the dev service
     */
    public Map<String, String> getExposedConfig() {
        Map<String, String> exposed = new HashMap<>(1);
        exposed.put(this.config.serviceName() + ".tcp.port", Objects.toString(getFirstMappedPort()));
        exposed.put(this.config.serviceName() + ".tcp.host", Objects.toString(getEffectiveHost()));
        exposed.put("quarkus.antivirus.clamav.port", Objects.toString(getFirstMappedPort()));
        exposed.put("quarkus.antivirus.clamav.host", Objects.toString(getEffectiveHost()));
        exposed.putAll(super.getEnvMap());
        return exposed;
    }

    public String getEffectiveHost() {
        if (useSharedNetwork) {
            return hostName;
        }

        return getHost();
    }

    /**
     * Use "quarkus.antivirus.clamav.port" to configure ClamAV as its exposed TCP port.
     *
     * @return the port or 3310 if not found which will cause this service not to start
     */
    public int getEffectivePort() {
        if (useSharedNetwork) {
            return PORT_TCP;
        }

        return getPort();
    }

    /**
     * Use "quarkus.antivirus.clamav.port" to configure ClamAV as its exposed TCP port.
     *
     * @return the port or 3310 if not found which will cause this service not to start
     */
    public static Integer getPort() {
        return ConfigProvider.getConfig().getOptionalValue("quarkus.antivirus.clamav.port",
                Integer.class).orElse(PORT_TCP);
    }

    /**
     * Use "quarkus.antivirus.clamav.host" to configure ClamAV server.
     *
     * @return the host in an Optional.
     */
    public static Optional<String> getTcpHost() {
        return ConfigProvider.getConfig().getOptionalValue("quarkus.antivirus.clamav.host", String.class);
    }
}