package io.quarkiverse.antivirus.deployment.devui;

import java.util.Map;
import java.util.Optional;

import io.quarkiverse.antivirus.deployment.ClamAVContainer;
import io.quarkiverse.antivirus.deployment.ClamAVDevServicesConfigBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.ExternalPageBuilder;
import io.quarkus.devui.spi.page.FooterPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.WebComponentPageBuilder;

/**
 * Dev UI card for displaying important details such Mailpit embedded UI.
 */
public class AntivirusDevUIProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer,
            Optional<ClamAVDevServicesConfigBuildItem> configProps,
            BuildProducer<FooterPageBuildItem> footerProducer) {
        if (configProps.isPresent()) {
            Map<String, String> config = configProps.get().getConfig();
            final CardPageBuildItem card = new CardPageBuildItem();

            // ClamAV
            if (config.containsKey("clamav.tcp.port")) {
                final ExternalPageBuilder versionPage = Page.externalPageBuilder("ClamAV Port")
                        .icon("font-awesome-solid:virus-slash")
                        .url("https://www.clamav.net/")
                        .staticLabel(config.getOrDefault("clamav.tcp.port", ClamAVContainer.PORT_TCP.toString()));
                card.addPage(versionPage);

                // ClamAV Container Log Console
                WebComponentPageBuilder mailLogPageBuilder = Page.webComponentPageBuilder()
                        .icon("font-awesome-solid:virus-slash")
                        .title("ClamAV")
                        .componentLink("qwc-antivirus-log.js");

                footerProducer.produce(new FooterPageBuildItem(mailLogPageBuilder));
            }

            card.setCustomCard("qwc-antivirus-card.js");
            cardPageBuildItemBuildProducer.produce(card);
        }
    }
}