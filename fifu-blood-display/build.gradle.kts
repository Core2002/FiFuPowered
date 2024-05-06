apply{
    plugin("org.jetbrains.kotlin.jvm")
}

tasks.withType<Jar>{
    manifest{
        attributes["Main-Class"] = "fun.fifu.bloodvolumedisplay.Main"
    }
}

