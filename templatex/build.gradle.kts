import java.util.*

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.6.0"
}

group = "com.pingfangx.plugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2021.2")
    type.set("IC")

    plugins.set(listOf("android"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("212")
        untilBuild.set("222.*")
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
        jvmArgs("-Xmx1G")
    }
}
