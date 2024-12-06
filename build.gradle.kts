plugins {
    kotlin("jvm") version "2.0.21"
}

group = "io.github.klahap"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
        freeCompilerArgs.add("-Xopt-in=kotlinx.coroutines.DelicateCoroutinesApi")
        freeCompilerArgs.add("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
    }
}
