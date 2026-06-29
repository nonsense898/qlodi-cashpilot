rootProject.name = "qlodi-cashpilot"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// Скелет: ядро домену (леджер) + спільний UI + застосунок.
// Далі за патерном frc-business додаються :core:data та :features:* модулі.
include(":composeApp")
include(":core:domain")
include(":shared:ui")
