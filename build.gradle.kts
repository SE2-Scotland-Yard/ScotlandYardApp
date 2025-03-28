// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "5.1.0.4882"
}
sonar {
    properties {
        property("sonar.projectKey", "SE2-Scotland-Yard_ScotlandYardApp")
        property("sonar.organization", "se2-scotland-yard")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}