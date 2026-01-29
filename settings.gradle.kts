rootProject.name = "Herocraft"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("kotlincrypto") {
            from("org.kotlincrypto:version-catalog:0.7.2")
        }
        create("awssdk") {
            from("aws.sdk.kotlin:version-catalog:1.6.5")
        }
    }
}