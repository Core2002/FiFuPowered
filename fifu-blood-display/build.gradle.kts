plugins{
    kotlin("jvm") version "2.0.21"
}

tasks.withType<Jar>{
    manifest{
        attributes["Main-Class"] = "fun.fifu.bloodvolumedisplay.Main"
    }
}

