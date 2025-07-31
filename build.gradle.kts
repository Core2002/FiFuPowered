import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val kotlin_version: String by project
val junit_version: String by project
val lombok_version: String by project

plugins {
    kotlin("jvm") version "2.1.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    group = "fun.fifu.powered"
    version = "1.21.4-SNAPSHOT"

    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://maven.aliyun.com/repository/apache-snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://libraries.minecraft.net/")
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("com.github.johnrengelman.shadow")
    }

    val api_version = "1.21"

    dependencies {
        implementation(files("./lib"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
        compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
        compileOnly("org.projectlombok:lombok:$lombok_version")
    }

    tasks {
        named<ShadowJar>("shadowJar") {
            archiveClassifier.set("")
        }
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
        register<Copy>("moveJarToOutputDir") {
            dependsOn("jar")
            val outputDir = file("$rootDir/build/libs")
            val shadowJarTask = project.tasks.findByPath("shadowJar") as ShadowJar
            from(shadowJarTask.outputs.files)
            into(outputDir)
        }
        build {
            dependsOn(shadowJar)
            dependsOn("moveJarToOutputDir")
        }
        test {
            useJUnitPlatform()
        }
    }
}
