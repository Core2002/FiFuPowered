val kotlin_version: String by project
val junit_version:String by project
val lombok_version:String by project

plugins {
    kotlin("jvm") version "1.9.23"
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

subprojects {
    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
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
        implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
        implementation(fileTree("./lib"))

        testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")

        compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
        compileOnly("org.projectlombok:lombok:$lombok_version")
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
