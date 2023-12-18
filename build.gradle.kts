plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}


subprojects {
    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            val prefix = "plugin:androidx.compose.compiler.plugins.kotlin"
            val buildDir = project.buildDir.absolutePath
            val projectDir = project.projectDir.absolutePath

            freeCompilerArgs += listOf(
                "-P",
                "${prefix}:reportsDestination=${buildDir}/compose_compiler",
                "-P",
                "${prefix}:metricsDestination=${buildDir}/compose_compiler",
                "-P",
                "${prefix}:stabilityConfigurationPath=${projectDir}/stability-config.txt",
            )
        }
    }
}
