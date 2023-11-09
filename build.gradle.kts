import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.vanillamodifier"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven {
        url = uri("https://repo.marcloud.net/")
        name = "marCloud-Repository"
    }
}

dependencies {
    implementation("org.ow2.asm:asm:9.5")
    implementation("org.ow2.asm:asm-tree:9.5")
    implementation("org.ow2.asm:asm-util:9.5")
    implementation("org.ow2.asm:asm-analysis:9.5")
    implementation("org.ow2.asm:asm-commons:9.5")
    implementation("com.github.cubk1:EventManager:-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:2.8.8")
    compileOnly("org.lwjgl:lwjgl:2.9.4-nightly")
    compileOnly("org.lwjgl:util:2.9.4-nightly")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.getByName<ShadowJar>("shadowJar") {
    dependencies {
        exclude("com.google.code.gson")
        exclude("org.lwjgl:util")
        exclude("org.lwjgl:lwjgl")
    }
}