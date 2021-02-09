package com.devfactory.eng

import org.gradle.api.publish.PublishingExtension
import java.net.URI

fun PublishingExtension.defaultPublishingRepository(version: Any) {
    repositories {
        maven {
            name = "sonatype"
            val repo = if(version.toString().endsWith("SNAPSHOT"))
                "https://oss.sonatype.org/content/repositories/snapshots" else
                "https://oss.sonatype.org/service/local/staging/deploy/maven2"

            url = URI(repo)

            credentials {
                username = System.getProperty("SONATYPE_USER")
                password = System.getProperty("SONATYPE_PASSWORD")
            }
        }
    }
}
