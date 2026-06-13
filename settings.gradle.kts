pluginManagement {
    repositories {
//        maven(url = uri("https://maven.myket.ir"))
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
//        maven(url = uri("https://maven.myket.ir"))
        mavenCentral()
    }
}
rootProject.name = "Shop"
