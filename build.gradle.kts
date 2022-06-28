plugins {
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("java-library")
    id("maven-publish")
}

group = "org.taruts.djig"
version = "001"

java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations["annotationProcessor"])
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    api("org.springframework:spring-core")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.7.0")
    }
}

publishing {
    publications {
        create<MavenPublication>("jar") {
            // We use the java-library plugin in this project. The java-library is based upon the java plugin.
            // During the build process, the java plugin creates a so-called component which is a collection of things to publish.
            // The maven-publish plugin can create publications from components.
            // that the maven-publish can use. The component is named "java" after the java plugin.
            from(components["java"])

            // Also we use the plugin io.spring.dependency-management.
            // This plugin enables us not to specify versions manually for those dependencies of the project
            // that Spring libraries work with.
            // But by default the dependency versions in the java component are those specified manually.
            // This configuration is needed to change this default.
            versionMapping {
                usage("java-api") {
                    fromResolutionResult()
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
    repositories {
        maven {
            name = "s3MavenRepo"
            url = uri("s3://maven.taruts.net")
            authentication {
                // AwsImAuthentication means that the credentials are in an AWS profile on the computer
                // Only the author of this project has those credentials and can upload dynamic-api artifacts
                register("aws", AwsImAuthentication::class)
            }
        }
    }
}
