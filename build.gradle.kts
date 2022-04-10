plugins {
    kotlin("jvm") version "1.6.20"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.github.proxyfox"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("io.github.proxyfox.Main")
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.steenooo:brigadierkt:v1.2.4")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.mojang:brigadier:1.0.18")
    implementation("dev.kord:kord-core:0.8.0-M12")
    implementation("org.slf4j:slf4j-nop:1.7.29")
}

tasks {
    jar {
        archiveClassifier.set("nodeps")
    }
    shadowJar {
        archiveClassifier.set("")
    }
}