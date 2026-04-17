plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("com.vanniktech.maven.publish") version "0.36.0"
}

kotlin {
    jvmToolchain(21)

    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlin.coroutines)

            api(libs.compose.runtime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies { }
        iosMain.dependencies { }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = "dev.voir",
        artifactId = "formica",
        version = project.version.toString()
    )

    pom {
        name.set("Formica")
        description.set("A Kotlin Multiplatform-friendly form engine")
        url.set("https://github.com/VoirDev/formica/")

        licenses {
            license {
                name.set("GNU Lesser General Public License, Version 3")
                url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
            }
        }

        developers {
            developer {
                id.set("checksanity")
                name.set("Gary Bezruchko")
                email.set("hello@voir.dev")
                organization.set("VOIR")
                organizationUrl.set("https://voir.dev")
            }
        }

        scm {
            url.set("https://github.com/VoirDev/formica/")
            connection.set("scm:git:git://github.com/VoirDev/formica.git")
            developerConnection.set("scm:git:ssh://git@github.com/VoirDev/formica.git")
        }
    }
}
