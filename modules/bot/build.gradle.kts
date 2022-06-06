plugins {
    kotlin("jvm") version "1.6.20"
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:database"))

    implementation(kotlin("stdlib"))

    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("dev.kord:kord-core:0.8.0-M12")
}