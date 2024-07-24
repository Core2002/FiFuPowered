plugins {
    kotlin("jvm")
}

tasks.withType<Jar>{
    manifest{
        attributes["Main-Class"] = "fun.fifu.serverbackup.Main"
    }
}
