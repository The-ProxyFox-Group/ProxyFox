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

    implementation(kotlin("stdlib"))

    implementation("dev.kord:kord-core:0.8.0-M12")
    implementation("com.vladsch.kotlin-jdbc:kotlin-jdbc:0.5.2")
    implementation("org.postgresql:postgresql:42.3.3")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("org.litote.kmongo:kmongo:4.6.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.6.0")
    implementation("org.litote.kmongo:kmongo-async:4.6.0")
}