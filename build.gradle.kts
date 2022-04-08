plugins {
    kotlin("jvm") version "1.6.20"
}

group = "io.github.proxyfox"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.mojang:brigadier:1.0.18")
    implementation("dev.kord:kord-core:0.8.0-M12")
    implementation("com.github.steenooo:brigadierkt:v1.2.4")
}