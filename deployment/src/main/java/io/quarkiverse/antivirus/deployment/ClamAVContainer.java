package io.quarkiverse.antivirus.deployment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Testcontainers implementation for ClamAV antivirus server.
 * <p>
 * Supported image: {@code clamav/clamav}
 * Find more info about ClamAV on <a href="https://www.clamav.net/">https://www.clamav.net/</a>.
 * <p>
 * Exposed ports: 3310 (tcp)
 */
public final class ClamAVContainer extends GenericContainer<ClamAVContainer> {

    public static final String NAME = "clamav";

    /**
     * Logger which will be used to capture container STDOUT and STDERR.
     */
    private static final Logger log = Logger.getLogger(ClamAVContainer.class);

    /**
     * Default ClamAV TCP port.
     */
    public static final Integer PORT_TCP = Integer.parseInt(ClamAVBuildConfig.PORT_TCP);

    ClamAVContainer(ClamAVBuildConfig config) {
        super(DockerImageName.parse(config.imageName()).asCompatibleSubstituteFor(ClamAVBuildConfig.DEFAULT_IMAGE));
        super.withLabel(ClamAVDevServicesProcessor.DEV_SERVICE_LABEL, NAME);
        super.withNetwork(Network.SHARED);
        super.waitingFor(Wait.forLogMessage(".*socket found, clamd started.*", 1));
        super.withEnv("CLAMD_STARTUP_TIMEOUT", Integer.toString(config.startupTimeout()));
        super.withEnv("CLAMAV_NO_FRESHCLAMD", Boolean.toString(!config.freshClam()));
        super.withEnv("CLAMAV_NO_CLAMD ", "false");
        super.withEnv("CLAMAV_NO_MILTERD", "true");
        super.withCopyFileToContainer(
                MountableFile.forClasspathResource("/clamd.conf"),
                "/etc/clamav/clamd.conf");

        // forward the container logs
        if (config.logging()) {
            super.withLogConsumer(new JbossContainerLogConsumer(log).withPrefix(NAME));
        }
    }

    @Override
    protected void configure() {
        super.configure();

        // this forces the TCP port to match quarkus.antivirus.clamav.port
        addFixedExposedPort(getPort(), PORT_TCP);
    }

    /**
     * Info about the DevService used in the DevUI.
     *
     * @return the map of as running configuration of the dev service
     */
    public Map<String, String> getExposedConfig() {
        Map<String, String> exposed = new HashMap<>(1);
        exposed.put(NAME + ".tcp.port", Objects.toString(getPort()));
        exposed.putAll(super.getEnvMap());
        return exposed;
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
     * @return the host or "localhost" if not found
     */
    public static String getTcpHost() {
        return ConfigProvider.getConfig().getOptionalValue("quarkus.antivirus.clamav.host",
                String.class).orElse("localhost");
    }
}