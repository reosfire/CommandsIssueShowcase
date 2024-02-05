plugins {
    kotlin("jvm") version "1.9.22"
}

group = "ru.reosfire"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("dev.hollowcube:minestom-ce:0494ee0b97")
    implementation("org.slf4j:slf4j-simple:2.0.11")
}