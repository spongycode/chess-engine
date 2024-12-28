// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

afterEvaluate {
    project.extensions.findByType(PublishingExtension::class.java)?.apply {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.spongycode"
                artifactId = "chess-engine"
                version = "1.0"
            }
        }
    }
}