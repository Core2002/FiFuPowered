import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val kotlin_version: String by project
val junit_version:String by project
val lombok_version:String by project

plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

subprojects {
    apply {
        plugin("java")
        plugin("com.github.johnrengelman.shadow")
    }

    group = "fun.fifu.powered"
    version = "1.0-SNAPSHOT"

    repositories {
        maven("https://papermc.io/repo/repository/maven-public/")
        google()
        mavenCentral()
    }

    dependencies {
        implementation(fileTree("./lib"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")

        compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
        compileOnly("org.projectlombok:lombok:$lombok_version")
    }

    tasks.withType<ShadowJar>{
        destinationDirectory = file("$rootDir/build/libs")
    }

    tasks {
        build {
            dependsOn(shadowJar)
        }
        test {
            useJUnitPlatform()
        }
    }
}
