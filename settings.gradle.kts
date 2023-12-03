pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            version("protobuf", "3.21.7")
            version("compose-ui", "1.5.4")
            version("compose-kotlinCompilerExtension", "1.5.5")

            // https://developer.android.com/jetpack/compose/bom/bom-mapping
            library(
                "compose-bom",
                "androidx.compose:compose-bom:2023.10.01",
            )
            library(
                "core-ktx",
                "androidx.core:core-ktx:1.12.0"
            )
            library(
                "activity-compose",
                "androidx.activity:activity-compose:1.8.1"
            )
            library(
                "compose-ui",
                "androidx.compose.ui",
                "ui"
            ).versionRef("compose-ui")
            library(
                "compose-ui-graphics",
                "androidx.compose.ui",
                "ui-graphics"
            ).versionRef("compose-ui")
            library(
                "compose-ui-tooling",
                "androidx.compose.ui",
                "ui-tooling"
            ).versionRef("compose-ui")
            library(
                "datastore",
                "androidx.datastore:datastore:1.0.0"
            )
            library(
                "protobuf-protoc",
                "com.google.protobuf",
                "protoc"
            ).versionRef("protobuf")
            library(
                "protobuf-javalite",
                "com.google.protobuf",
                "protobuf-javalite"
            ).versionRef("protobuf")
            library(
                "compose-material3",
                "androidx.compose.material3:material3:1.1.2"
            )
            library(
                "lifecycle-viewmodel-compose",
                "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2"
            )
            library(
                "lifecycle-runtime-ktx",
                "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
            )
            library(
                "lifecycle-viewmodel-ktx",
                "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2"
            )
            library(
                "lifecycle-livedata-ktx",
                "androidx.lifecycle:lifecycle-livedata-ktx:2.6.2"
            )
            library(
                "lifecycle-extensions",
                "androidx.lifecycle:lifecycle-extensions:2.2.0"
            )
            library(
                "navigation-compose",
                "androidx.navigation:navigation-compose:2.7.5"
            )
            library(
                "junit",
                "org.junit:junit-bom:5.10.1"
            )
            library(
                "junit-ktx",
                "androidx.test.ext:junit-ktx:1.1.5"
            )
            library(
                "espresso-core",
                "androidx.test.espresso:espresso-core:3.5.1"
            )
            library(
                "compose-ui-test-junit",
                "androidx.compose.ui",
                "ui-test-junit5"
            ).versionRef("compose-ui")
            library(
                "compose-ui-test-manifest",
                "androidx.compose.ui",
                "ui-test-manifest"
            ).versionRef("compose-ui")
        }
    }
}

include(":app")

rootProject.name = "ATBL"