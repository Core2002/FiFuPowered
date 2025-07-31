plugins{
    kotlin("jvm") version "2.1.21"
}

tasks.withType<Jar>{
    manifest{
        attributes["Main-Class"] = "fun.fifu.bloodvolumedisplay.Main"
    }
}

