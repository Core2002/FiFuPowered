plugins {
    kotlin("jvm")
}

dependencies {
    // Minecraft API
//    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    
    // 核心依赖
    implementation("org.apache.commons:commons-compress:1.26.2")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.vertx:vertx-core:4.5.9")
    implementation("io.vertx:vertx-web:4.5.9")
    implementation("dev.samstevens.totp:totp:1.7.1")
    implementation("commons-net:commons-net:3.9.0")
    implementation("commons-cli:commons-cli:1.9.0")
    
    // 测试依赖
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "fun.fifu.serverbackup.Main"
    }
    
    // 包含依赖
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
