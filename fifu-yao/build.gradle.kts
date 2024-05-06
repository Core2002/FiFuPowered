apply{
}

tasks.withType<Jar>{
    manifest{
        attributes["Main-Class"] = "fun.fifu.yao.Main"
    }
}

