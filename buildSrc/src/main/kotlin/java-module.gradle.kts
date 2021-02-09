import com.devfactory.eng.defaultPublishingRepository

plugins {
    `java-library`
    `maven-publish`
    id("io.freefair.lombok")
}

repositories {
    jcenter()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.mockito:mockito-core:3.6.28")
    testImplementation("org.assertj:assertj-core:3.18.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}

publishing {
    defaultPublishingRepository(project.version)

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
