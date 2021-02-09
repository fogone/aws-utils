plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("io.freefair.gradle:lombok-plugin:5.3.0")
}

fun plugin(name: String, version: String): String {
    return "$name:$name.gradle.plugin:$version"
}
