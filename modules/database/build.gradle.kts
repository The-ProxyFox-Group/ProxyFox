plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    shadow(project(":modules:common"))?.let { implementation(it) }
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")?.let { implementation(it) }
    shadow("ch.qos.logback:logback-classic:1.2.11")?.let { implementation(it) }
    shadow("com.google.guava:guava:31.1-jre")?.let { implementation(it) }
    shadow("com.google.code.gson:gson:2.9.0")?.let { implementation(it) }
    shadow("dev.kord:kord-core:0.8.0-M16")?.let { implementation(it) }
    shadow("com.vladsch.kotlin-jdbc:kotlin-jdbc:0.5.2")?.let { implementation(it) }
    shadow("org.postgresql:postgresql:42.3.3")?.let { implementation(it) }
    shadow("org.litote.kmongo:kmongo:4.6.0")?.let { implementation(it) }
    shadow("org.litote.kmongo:kmongo-coroutine:4.6.0")?.let { implementation(it) }
    shadow("org.litote.kmongo:kmongo-async:4.6.0")?.let { implementation(it) }
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "dev.proxyfox.database.DatabaseMainKt"
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    classifier = "shadow"
    mergeServiceFiles()
}
tasks.build {
    dependsOn("shadowJar")
}