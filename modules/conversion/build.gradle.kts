plugins {
    kotlin("jvm") version "1.7.10"
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
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "dev.proxyfox.conversion.ConversionMainKt"
    }
}
