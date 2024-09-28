package `fun`.fifu.serverbackup

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            DataServer.startHTTPServer(6542, -1)
            println("有没有一种可能，这只是一只普通的Minecraft插件捏qwq")
        }
    }
}
