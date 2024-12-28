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

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.spongycode"
            artifactId = "chess-engine"
            version = "1.0.0"
            from(components["java"])
        }
    }
}
