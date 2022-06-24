plugins {
    kotlin("jvm") version "1.6.20"
    application
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

application {
    mainClass.set("dev.proxyfox.api.server.ServerMain")
}

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:database"))

    implementation(kotlin("stdlib"))
}