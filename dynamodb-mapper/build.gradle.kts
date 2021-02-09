plugins {
    `java-module`
}

repositories {
    mavenCentral()
}

dependencies {
    api("software.amazon.awssdk:dynamodb:2.15.61")
    compileOnly("javax.inject:javax.inject:1")
}

