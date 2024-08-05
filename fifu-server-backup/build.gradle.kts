plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.apache.commons:commons-compress:1.26.2")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.vertx:vertx-core:4.5.9")
    implementation("io.vertx:vertx-web:4.5.9")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "fun.fifu.serverbackup.Main"
    }
}
