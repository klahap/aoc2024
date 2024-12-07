import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("multiplatform") version "2.1.0"
}
val kotlinVersion = "2.1.0"
val kotlinxCoroutineVersion = "1.9.0"

group = "io.github.klahap"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    macosArm64 {
        binaries {
            executable(listOf(RELEASE)) {
                entryPoint = "io.github.klahap.main"
                freeCompilerArgs = listOf(
                    "-opt",
                    "-Xbinary=gc=cms",
                )
            }
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("kotlinx.coroutines.DelicateCoroutinesApi")
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
        val commonMain by getting {
            compilerOptions {
                languageVersion.set(KotlinVersion.KOTLIN_2_1)
            }
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.6.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutineVersion")
                implementation("io.kotest:kotest-assertions-core:5.9.0")
            }
        }
    }
}
