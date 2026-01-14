plugins {
    kotlin("jvm") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "com.b93"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
}

kotlin {
    jvmToolchain(21)
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}

