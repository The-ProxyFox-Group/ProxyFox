plugins {
    kotlin("jvm") version "1.7.10"
    application
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

application {
    mainClass.set("dev.proxyfox.bot.BotMain")
}

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:database"))

    implementation(kotlin("stdlib"))

    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("dev.kord:kord-core:0.8.0-M16")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "dev.proxyfox.bot.BotMainKt"
    }
}