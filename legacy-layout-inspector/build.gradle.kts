import java.util.*

plugins {
    id("java") // java
    id("org.jetbrains.kotlin.jvm") version "1.8.22" // kotlin
    id("org.jetbrains.intellij") version "1.10.1" // intellij
}

group = "com.pingfangx.plugin"
version = "1.2.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    // The version of IntelliJ IDEA Community Edition that Android Studio Hedgehog based on
    // https://plugins.jetbrains.com/docs/intellij/android-studio.html#android-studio-releases-listing
    version.set("AI-2023.1.1.26")

    plugins.set(listOf("android"))
}
tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        // 212      1.0.0
        // 213-223  1.1.0
        // 231-     1.2.0
        sinceBuild.set("231")
        // set untilBuild undefined, update the plugin if it causes incompatibility errors
        // tested Hedgehog | 2023.1.1, Iguana | 2023.2.1 Canary 18
        untilBuild.set("")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    runIde {
        // Absolute path to installed target 3.5 Android Studio to use as
        // IDE Development Instance (the "Contents" directory is macOS specific):
        val properties = Properties().apply {
            val file = File(rootDir, "local.properties")
            if (file.isFile) {
                file.inputStream().use {
                    load(it)
                }
            }
        }
        properties.getProperty("StudioRunPath")?.let {
            logger.lifecycle("config ideDir: $it")
            ideDir.set(file(it))
        }
    }
    buildSearchableOptions {
        // May be affected by intellij.version, disable it.
        enabled = false
    }
}
