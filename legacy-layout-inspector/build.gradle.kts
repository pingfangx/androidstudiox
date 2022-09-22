import java.util.*

plugins {
    id("java") // java
    id("org.jetbrains.kotlin.jvm") version "1.6.20" // kotlin
    id("org.jetbrains.intellij") version "1.6.0" // intellij
}

group = "com.pingfangx.plugin"
version = "1.1.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("213.7172.25") // for Android Studio Dolphin
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("android"))
}
tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
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
}
