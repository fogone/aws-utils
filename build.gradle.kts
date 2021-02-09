import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig

plugins {
    id("pl.allegro.tech.build.axion-release") version "1.10.2"
}

scmVersion {
    tag(closureOf<TagNameSerializationConfig> {
        prefix = "release"
        versionSeparator = "/"
    })
}

val realVersion = scmVersion.version

allprojects {
    group = "ru.nobirds.aws"
    version = realVersion
}

logger.quiet("Current version: ${scmVersion.version}")

