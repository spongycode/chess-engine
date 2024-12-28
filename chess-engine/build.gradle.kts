plugins {
    id("java-library")
    id("maven-publish")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

group = "com.github.spongycode"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(libs.junit)
}