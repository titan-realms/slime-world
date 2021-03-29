plugins {
    java
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

val minestomVersion: String by project;

group = "net.titanrealms.titan"
version = "0.1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://libraries.minecraft.net") }
    maven { url = uri("https://repo.spongepowered.org/maven") }
}

dependencies {
    compileOnly("com.github.Minestom:Minestom:$minestomVersion")
    implementation("com.github.luben:zstd-jni:1.4.9-1")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "net.titanrealms.titan"
            artifactId = "world"
            version = "0.1.0"

            from(components["java"])
        }
    }
}