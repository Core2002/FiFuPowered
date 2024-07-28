plugins {
    kotlin("jvm")
}

dependencies{
    implementation("org.apache.commons:commons-compress:1.26.2")
    implementation("com.google.code.gson:gson:2.10")
}

tasks.withType<Jar>{
    manifest{
        attributes["Main-Class"] = "fun.fifu.serverbackup.Main"
    }
}
