import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val kotlin_version: String by project
val junit_version: String by project
val lombok_version: String by project

plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

subprojects {
    apply {
        plugin("java")
        plugin("com.github.johnrengelman.shadow")
    }

    group = "fun.fifu.powered"
    version = "1.20.6-SNAPSHOT"
    val api_version = "1.19"

    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://maven.aliyun.com/repository/apache-snapshots/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://libraries.minecraft.net/")
        mavenCentral()
    }

    dependencies {
        implementation(fileTree("./lib"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
        compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
        compileOnly("org.projectlombok:lombok:$lombok_version")
    }

    tasks.withType<ShadowJar> {
        destinationDirectory = file("$rootDir/build/libs")
    }

    tasks {
        register<Copy>("copyBinaryResources") {
            from("src/main/resources") {
                include("*.db")
            }
            into("build/resources/main")
        }
        processResources {
            dependsOn("copyBinaryResources")
            exclude("*.db")
            expand(
                "version" to project.version,
                "api_version" to api_version
            )
        }
        build {
            dependsOn(shadowJar)
        }
        test {
            useJUnitPlatform()
        }
    }
}
