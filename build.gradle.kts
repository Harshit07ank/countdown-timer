plugins {
    // No plugins at the root. App module will apply Android and Kotlin plugins.
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Kept empty; using plugins DSL in modules.
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Enable Kotlin incremental compilation and other common settings
subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjvm-default=all",
                "-Xcontext-receivers"
            )
        }
    }
}