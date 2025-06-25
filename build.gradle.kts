plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.0"
}

group = "com.yourplugin"
version = "1.0.2"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // ✅ Add this
    implementation("org.json:json:20240303")

}


intellij {
    version.set("2022.3") //
    type.set("IC") // Community Edition
    plugins.set(listOf("java"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("223")  // for IntelliJ 2021.1
        untilBuild.set("231.*") // adjust as needed
    }

    buildPlugin {
        dependsOn("jar")
    }
}
