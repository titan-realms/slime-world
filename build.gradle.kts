plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

val minestomVersion: String by project;

group = "net.titanrealms.slime"
version = "0.1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://libraries.minecraft.net") }
    maven { url = uri("https://repo.spongepowered.org/maven") }
}

dependencies {
    implementation("com.github.Minestom:Minestom:$minestomVersion")
    implementation("com.github.luben:zstd-jni:1.4.9-1")
}