val kotlin_version: String by project
val hutool_version: String by project

apply{
    plugin("org.jetbrains.kotlin.jvm")
}

dependencies{
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")

    implementation("cn.hutool:hutool-all:$hutool_version")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
}

tasks.withType<Jar>{
    manifest{
        attributes["Main-Class"] = "fun.fifu.fifusky.Main"
    }
}
