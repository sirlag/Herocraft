rootProject.name = "Herocraft"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("kotlincrypto") {
            from("org.kotlincrypto:version-catalog:0.8.0")
        }
        create("awssdk") {
            from("aws.sdk.kotlin:version-catalog:1.5.85")
        }
    }
}