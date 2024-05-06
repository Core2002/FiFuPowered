dependencies{
}

tasks.withType<Jar>{
    manifest{
        attributes["Main-Class"] = "fun.fifu.mininglist.Main"
    }
}

