import java.util.*

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.6.0"
}

val templatexPluginId = "com.pingfangx.plugin.templatex"
group = "com.pingfangx.plugin"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2021.2")
    type.set("IC")

    plugins.set(listOf("android"))
}

val localProperties = Properties().apply {
    val file = File(rootDir, "local.properties")
    if (file.isFile) {
        file.inputStream().use {
            load(it)
        }
    }
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
        localProperties.getProperty("StudioRunPath")?.let {
            ideDir.set(file(it))
            log("config ideDir = $it")
        }
        jvmArgs("-Xmx1G")
    }
}

localProperties.getProperty("templates")?.toString()?.let {
    configTemplates(it)
} ?: run {
    sourceSets.getByName("main").resources.srcDir("src/templates-samples/resources") // add samples
}
fun configTemplates(templates: String?) {
    templates ?: return
    logConfig("templates", templates)

    intellij {
        pluginName.set("${project.name}-$templates")
        logConfig("pluginName", pluginName)
    }
    sourceSets {
        getByName("main") {
            java.setSrcDirs(setOf("src/$templates/kotlin"))
            resources.setSrcDirs(setOf("src/templates-common/resources")) // set common
            resources.srcDir("src/$templates/resources") // add flavor specific
            resources.srcDir("src/templates-$templates/resources") // add flavor specific
            // resources.exclude("src/main/resources/messages/TemplateXBundle.properties") // exclude messages, not work
            logConfig("java.srcDirs", java.srcDirs)
            logConfig("resources.srcDirs", resources.srcDirs)
        }
    }
    tasks {
        patchPluginXml {
            pluginId.set("$templatexPluginId.${templates.replace("-", ".")}")
            logConfig("pluginId", pluginId)
            pluginDescription.set("<li>$templates</li>A group of templates used by the templatex plugin.")
            logConfig("pluginDescription", pluginDescription)
        }
        withType(Jar::class.java) {
            archiveBaseName.set(intellij.pluginName)
            exclude("com/**") // java.setSrcDirs includes src/main/kotlin by default (because of the Kotlin plugin)
            exclude("**/messages/**") // exclude messages, sourceSets.main.resources.exclude not work
            exclude("**/META-INF/*.kotlin_module") // exclude .kotlin_module
        }
    }
}

fun logConfig(config: String, any: Any) {
    log(
        "config $config = " + if (any is Provider<*>) {
            any.get()
        } else {
            any.toString()
        }
    )
}

fun log(any: Any) {
    logger.lifecycle(any.toString())
}
