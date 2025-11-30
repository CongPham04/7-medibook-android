pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")

            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        google()
        mavenCentral()
        // Thêm Cloudinary
        maven {
            url = uri("https://linkedin.jfrog.io/artifactory/litr-repo")
        }
        // Thư viện OTP
//        maven {
//            url = uri("https://jitpack.io")
//        }
    }
}

rootProject.name = "MedibookAndroid"
include(":app")
 