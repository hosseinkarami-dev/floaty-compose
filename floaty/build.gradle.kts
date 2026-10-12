import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    `maven-publish`
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Floaty")
            description.set("Customizable in-app notifications for Compose Multiplatform.")
            url.set("https://github.com/hosseinkarami-dev/floaty-compose")
            licenses {
                license {
                    name.set("The MIT License")
                    url.set("https://opensource.org/license/mit")
                    distribution.set("repo")
                }
            }
            scm {
                url.set("https://github.com/hosseinkarami-dev/floaty-compose")
                connection.set("scm:git:git://github.com/hosseinkarami-dev/floaty-compose.git")
                developerConnection.set("scm:git:ssh://git@github.com/hosseinkarami-dev/floaty-compose.git")
            }
        }
    }
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework { baseName = "Floaty"; isStatic = true }
    }
    jvm()
    js { browser(); binaries.executable() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser(); binaries.executable() }
    android {
        namespace = "io.github.hosseinkarami_dev.floaty.library"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            implementation(libs.compose.material3)
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}
