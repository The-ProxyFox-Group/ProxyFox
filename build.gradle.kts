plugins {
    kotlin("jvm") version "1.6.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.proxyfox"
version = "1.0.0"
val ktor_version = "1.6.7"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

dependencies {
}

tasks {
    jar {
        archiveClassifier.set("nodeps")
    }
    shadowJar {
        archiveClassifier.set("")
    }
}