package `fun`.fifu.serverbackup

import org.apache.commons.cli.*

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val options = Options()

            val helpOption = Option("h", "help", false, "显示帮助信息")
            val serveOption = Option("s", "serve", false, "启动服务")

            options.addOption(helpOption)
            options.addOption(serveOption)

            val formatter = HelpFormatter()

            val parser: CommandLineParser = DefaultParser()
            val cmd = parser.parse(options, args)

            if (args.isEmpty() || cmd.hasOption(helpOption)) {
                formatter.printHelp("有没有一种可能，这只是一只普通的Minecraft插件捏qwq", options)
                return
            }

            if (cmd.hasOption(serveOption)) {
                DataServer.startHTTPServer(6542, -1)
                return
            }

        }
    }
}
