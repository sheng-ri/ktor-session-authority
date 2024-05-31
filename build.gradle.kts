import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.11"
    `maven-publish`
}

version = "1.1.0"

kotlin {
    compilerOptions.jvmTarget = JvmTarget.JVM_21
}

val aliyun: String? by project

repositories {
    aliyun?.let {
        maven(it)
    }
    mavenCentral()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sheng-ri/ktor-session-authority")
            credentials {
                username = "sheng-ri"
                password = (project.findProperty("gpr.key") ?: System.getenv("TOKEN")) as String
            }
        }
    }
    publications  {
        register<MavenPublication>("gpr") {
            from(components["kotlin"])
            artifact(
                tasks.kotlinSourcesJar
            )
        }
    }
}

// why this will include a source with main?
tasks.kotlinSourcesJar {
    from(sourceSets.main.get().allSource)
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-sessions-jvm")
    testImplementation("io.ktor:ktor-server-netty-jvm")
    testImplementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation(kotlin("test"))
}
