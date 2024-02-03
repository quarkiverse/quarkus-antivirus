package io.quarkiverse.antivirus.deployment;

import io.quarkiverse.antivirus.runtime.Antivirus;
import io.quarkiverse.antivirus.runtime.ClamAVEngine;
import io.quarkiverse.antivirus.runtime.ClamAVHealthCheck;
import io.quarkiverse.antivirus.runtime.VirusTotalEngine;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

/**
 * Main processor for the Antivirus extension.
 */
class AntivirusProcessor {

    private static final String FEATURE = "antivirus";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerBeans(BuildProducer<AdditionalBeanBuildItem> beans,
            ClamAVBuildConfig buildConfig) {
        beans.produce(AdditionalBeanBuildItem.builder().setUnremovable()
                .addBeanClasses(ClamAVEngine.class)
                .addBeanClasses(VirusTotalEngine.class)
                .addBeanClasses(Antivirus.class)
                .build());
    }

    @BuildStep
    HealthBuildItem addHealthCheck(ClamAVBuildConfig buildConfig) {
        return new HealthBuildItem(ClamAVHealthCheck.class.getName(), buildConfig.healthEnabled());
    }
}