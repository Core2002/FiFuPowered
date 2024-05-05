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
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
        implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
        implementation(fileTree("./lib"))
        implementation("cn.hutool:hutool-all:5.8.27")

        testImplementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
        testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

        compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
        compileOnly("org.projectlombok:lombok:1.18.24")
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
