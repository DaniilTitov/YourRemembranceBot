import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "technology.bear"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:4.5.0")
    implementation("com.natpryce:konfig:1.6.10.0")
    implementation("org.postgresql:postgresql:42.2.9")
    implementation("org.jetbrains.exposed:exposed:0.14.1")
}

tasks.withType<ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "technology.bear.bot.MainKt"
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
