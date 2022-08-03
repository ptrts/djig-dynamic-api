plugins {
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    `java-library`
    `maven-publish`
    signing
}

group = "org.taruts.djig"
version = "1.0.0"

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

configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("java") {
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

            pom {
                packaging = "jar"
                name.set("djig-dynamic-api")
                url.set("https://gitlab.com/pavel-taruts/demos/djig/dynamic-api")
                description.set(
                    """
                    A library that a dynamic API library part of a djig application must depend on
                    """
                )

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                scm {
                    connection.set("scm:git:https://gitlab.com/pavel-taruts/demos/djig/dynamic-api.git")
                    developerConnection.set("scm:git:https://gitlab.com/pavel-taruts/demos/djig/dynamic-api.git")
                    url.set("https://gitlab.com/pavel-taruts/demos/djig/dynamic-api")
                }

                developers {
                    developer {
                        id.set("@ptrtss")
                        name.set("Pavel Taruts")
                        email.set("ptrts@mail.ru")
                    }
                }
            }
        }
    }
    repositories {
        mavenLocal()
        maven {
            name = "OSSRH"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = project.properties["ossrh.username"].toString()
                password = project.properties["ossrh.password"].toString()
            }
        }
    }
}

configure<SigningExtension> {
    if (project.hasProperty("signing.secretKey")) {
        // It's CI, because there is a project property "signing.secretKey", which we only plan to supply as an environment variable on CI

        // Environment variable ORG_GRADLE_PROJECT_signing.keyId
        val defaultKeyId: String = project.property("signing.keyId") as String

        // Environment variable ORG_GRADLE_PROJECT_signing.password
        val defaultPassword: String = project.property("signing.password") as String

        // Environment variable ORG_GRADLE_PROJECT_signing.secretKey
        val defaultSecretKey: String = project.property("signing.secretKey") as String

        useInMemoryPgpKeys(defaultKeyId, defaultSecretKey, defaultPassword)
    } else {
        // It's a developer's computer

        useGpgCmd()
    }
    sign(publishing.publications["java"])
}
