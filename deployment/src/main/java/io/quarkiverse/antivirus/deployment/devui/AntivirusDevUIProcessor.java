package io.quarkiverse.antivirus.deployment.devui;

import java.util.Objects;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import com.github.toolarium.icap.client.ICAPClientFactory;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.ExternalPageBuilder;
import io.quarkus.devui.spi.page.FooterPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.WebComponentPageBuilder;
import io.quarkus.runtime.configuration.ConfigUtils;

/**
 * Dev UI card for displaying important details.
 */
public class AntivirusDevUIProcessor {

    private static final String CLAMAV_PORT = "quarkus.antivirus.clamav.port";

    @BuildStep(onlyIf = IsDevelopment.class)
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer,
            BuildProducer<FooterPageBuildItem> footerProducer) {
        Config runtimeConfig = ConfigProvider.getConfig();
        final boolean clamAvEnabled = runtimeConfig.getOptionalValue("quarkus.antivirus.clamav.enabled",
                Boolean.class).orElse(Boolean.FALSE);
        final boolean virusTotalEnabled = runtimeConfig.getOptionalValue("quarkus.antivirus.virustotal.enabled",
                Boolean.class).orElse(Boolean.FALSE);
        final boolean icapEnabled = runtimeConfig.getOptionalValue("quarkus.antivirus.icap.enabled",
                Boolean.class).orElse(Boolean.FALSE);

        if (!clamAvEnabled && !virusTotalEnabled && !icapEnabled) {
            return;
        }

        final CardPageBuildItem card = new CardPageBuildItem();

        if (clamAvEnabled) {
            final String portLabel = ConfigUtils.isPropertyNonEmpty(CLAMAV_PORT)
                    ? runtimeConfig.getValue(CLAMAV_PORT, String.class)
                    : "Dev Services";

            final ExternalPageBuilder versionPage = Page.externalPageBuilder("ClamAV Port")
                    .icon("font-awesome-solid:virus-slash")
                    .url("https://www.clamav.net/")
                    .staticLabel(portLabel);
            card.addPage(versionPage);

            WebComponentPageBuilder mailLogPageBuilder = Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:virus-slash")
                    .title("ClamAV")
                    .componentLink("qwc-antivirus-log.js");

            footerProducer.produce(new FooterPageBuildItem(mailLogPageBuilder));
        }

        if (virusTotalEnabled) {
            final ExternalPageBuilder versionPage = Page.externalPageBuilder("VirusTotal")
                    .icon("font-awesome-solid:virus-slash")
                    .url("https://www.virustotal.com/")
                    .staticLabel("v3");
            card.addPage(versionPage);
        }

        if (icapEnabled) {
            final ExternalPageBuilder versionPage = Page.externalPageBuilder("ICAP")
                    .icon("font-awesome-solid:virus-slash")
                    .url("https://www.ietf.org/rfc/rfc3507.txt")
                    .doNotEmbed()
                    .staticLabel(Objects.toString(ICAPClientFactory.class.getPackage().getImplementationVersion(), "?"));
            card.addPage(versionPage);
        }

        card.setCustomCard("qwc-antivirus-card.js");
        cardPageBuildItemBuildProducer.produce(card);
    }
}
