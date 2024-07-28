/*
 * Copyright (c) 2023 NekokeCore(Core2002@aliyun.com)
 * FiFuPowered is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package `fun`.fifu.bookedit

import `fun`.fifu.bookedit.BookEdit.Companion.pluginName
import `fun`.fifu.bookedit.BookOperator.copyBook
import `fun`.fifu.bookedit.BookOperator.exportBook
import `fun`.fifu.bookedit.BookOperator.importBook
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class BookCommand : TabExecutor {
    private val bookFiles = mutableListOf<String>()
    private val myDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日HH时")

    private val helpMassage = mapOf(
        "help" to "/book help [command] 来查看帮助",
        "copy-to-writable-book" to "/book copy-to-writable-book 把主手的 成书/书与笔 复制成 书与笔 然后返还给玩家",
        "export-book" to "/book export-book-to-file <file> 把主手的 成书/书与笔 导出书到文件",
        "import-book" to "/book import-book <file> 从文件导入 书与笔",
        "view-book" to "/book view-book <file> [player] 给玩家打开一本成书，若 player 未填写，则为命令发送者"
    )

    /**
     * Tab完成函数，用于根据玩家输入提供命令建议。
     *
     * @param p0 命令发送者，可以是玩家或服务器本身。
     * @param p1 命令对象。
     * @param p2 玩家输入的命令字符串。
     * @param p3 命令参数数组。
     * @return 返回一个字符串列表，包含可能的命令完成建议。
     */
    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): MutableList<String> {
        if (p0 !is Player) return mutableListOf()
        if (p3.size == 1) return helpMassage.keys.toMutableList()
        val ml = mutableListOf<String>()
        val playersName = mutableListOf<String>()
        Bukkit.getOnlinePlayers().forEach {
            playersName.add(it.name)
        }

        return when (p3[0]) {
            "import-book", "view-book" -> {
                bookFiles.clear()
                File("plugins/$pluginName/").listFiles()?.forEach {
                    if (it.isFile)
                        bookFiles.add(it.nameWithoutExtension)
                }
                bookFiles.distinct()
                bookFiles
            }

            "export-book" -> {
                bookFiles.clear()
                File("plugins/$pluginName/").listFiles()?.forEach {
                    if (it.isFile)
                        bookFiles.add(it.nameWithoutExtension.split('@')[0])
                }
                bookFiles.distinct()
                bookFiles
            }

            "help" -> {
                ml.addAll(helpMassage.keys)
                ml
            }

            else -> ml
        }
    }

    /**
     * 处理命令的函数。
     * 当玩家或其他发送者发送一个命令时，这个函数将被调用。它根据命令的不同执行相应的逻辑。
     *
     * @param p0 命令的发送者。
     * @param p1 命令的具体实例。
     * @param p2 命令的原始字符串。
     * @param p3 命令的参数数组。
     * @return 命令是否被成功处理。
     */
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
//        println(
//            """
//            p0:$p0
//            p1:$p1
//            p2:$p2
//            p3:${p3.contentToString()}
//        """.trimIndent()
//        )
        if (p0 !is Player) {
            p0.sendMessage("你必须是一名玩家")
            return true
        }
        if (p3.isEmpty()) return onHelp(p0, p3)
        try {
            val re = when (p3[0]) {
                "help" -> onHelp(p0, p3)
                "copy-to-writable-book" -> copyToWritableBook(p0)
                "export-book" -> exportBookToFile(p0, p3)
                "import-book" -> importBookFromFile(p0, p3)
                "view-book" -> viewBook(p0, p3)
                else -> false
            }
            if (!re) onHelp(p0, arrayOf("help", p3[0]))
        } catch (e: Exception) {
            onHelp(p0, arrayOf("help", p3[0]))
            BookEdit.fs.logger.warning("$p0 的命令 /book ${p3.contentToString()} 导致了一个异常：")
            e.printStackTrace()
            return true
        }
        return true

    }

    /**
     * 让玩家查看一本书。
     *
     * @param p0 当前操作的玩家对象。
     * @param p3 一个字符串数组，包含操作指令的后续参数。第一个参数无用，第二个参数是书名，
     *           如果有第三个参数，则表示目标玩家的名称，用于OP玩家给其他玩家看书。
     * @return 总是返回true，表示操作已经被处理。
     *
     * 此函数首先检查参数数量，如果不满足条件则直接返回false或true。
     * 然后根据参数判断是否需要打开书籍，如果是OP玩家，还可以指定其他玩家打开书籍。
     */
    private fun viewBook(p0: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1)
            return false
        if (!bookFiles.contains(p3[1]))
            return true

        if (p3.size <= 2) {
            p0.openBook(importBook("plugins/$pluginName/${p3[1].filt()}.txt", Material.WRITTEN_BOOK))
            p0.sendMessage("正在浏览书 ${p3[1].filt()}")
        } else {
            if (p0.isOp) {
                val player = Bukkit.getPlayer(p3[2])
                player?.openBook(importBook("plugins/$pluginName/${p3[1].filt()}.txt", Material.WRITTEN_BOOK))
                player?.sendMessage("正在浏览书 ${p3[1].filt()}")
            } else {
                p0.sendMessage("只有OP才有这个权限")
            }
        }
        return true
    }

    /**
     * 从文件导入书籍到玩家的库存中。
     *
     * @param p0 玩家对象，书籍将被导入到这个玩家的库存中。
     * @param p3 包含导入命令参数的字符串数组，其中第二个元素是书籍的文件名。
     * @return 如果成功导入书籍，则返回true；如果文件名不正确或书籍已存在，则返回false。
     *
     * 此函数首先检查数组p3的长度是否为1，如果是，则表示没有提供正确的文件名，因此直接返回false。
     * 接着，它检查书籍文件名是否存在于bookFiles中，如果不存在，表示该书籍不需要导入，直接返回true。
     * 如果文件名存在，那么它会尝试导入书籍。书籍文件路径是基于插件名称和提供的文件名构建的。
     * 成功导入书籍后，它会将书籍添加到玩家的库存中，并通过发送消息通知玩家导入成功。
     * 最后，函数返回true，表示书籍成功导入。
     */
    private fun importBookFromFile(p0: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1)
            return false
        if (!bookFiles.contains(p3[1]))
            return true
        p0.inventory.addItem(importBook("plugins/$pluginName/${p3[1].filt()}.txt"))
        p0.sendMessage("成功导入书 ${p3[1].filt()}")
        return true
    }

    /**
     * 将玩家主手持有的书本导出到文件。
     *
     * 此函数检查玩家主手是否持有书本（或书与笔），如果是，则将该书本的内容导出到一个文本文件中。
     * 导出的文件名基于玩家名称、当前时间以及书本的标题生成，存储在plugins目录下。
     *
     * @param p0 当前玩家对象，必须持有书本才能导出。
     * @param p3 一个字符串数组，其中p3[1]被用作导出文件名的一部分。
     * @return 总是返回true，表示导出操作已启动（尽管实际成功与否取决于后续逻辑）。
     */
    private fun exportBookToFile(p0: Player, p3: Array<out String>): Boolean {
        val bookMeta = p0.inventory.itemInMainHand.itemMeta ?: {
            p0.sendMessage("你主手必须持有 书/书与笔")
        }
        if (bookMeta is BookMeta) {
            bookMeta.exportBook(
                "plugins/$pluginName/${p3[1].filt()}@${p0.name}_${
                    myDateTimeFormatter.format(
                        LocalDateTime.now()
                    )
                }.txt"
            )
            p0.sendMessage("成功导出书 ${p3[1].filt()}")
        }
        return true
    }

    /**
     * 将玩家主手中的书复制到玩家的背包中。
     *
     * @param p0 需要复制书的玩家。
     * @return 总是返回true，表示函数执行成功。
     *
     * 函数首先检查玩家主手中的物品是否是书（BookMeta）类型，
     * 如果不是，则向玩家发送错误消息要求他们持有书。
     * 如果是书，则创建该书的复制品并添加到玩家的背包中，
     * 同时向玩家发送成功复制书的消息。
     */
    private fun copyToWritableBook(p0: Player): Boolean {
        val bookMeta = p0.inventory.itemInMainHand.itemMeta ?: {
            p0.sendMessage("你主手必须持有 书/书与笔")
        }
        if (bookMeta is BookMeta) {
            p0.inventory.addItem(bookMeta.copyBook())
            p0.sendMessage("成功复制书")
        }
        return true
    }


    /**
     * 处理玩家的帮助请求。
     * 当玩家输入特定的帮助命令时，此函数被调用，用于提供有关命令的详细信息或显示所有可用命令的列表。
     *
     * @param player 请求帮助的玩家对象。
     * @param p3 帮助命令的参数数组，其中p3[1]用于指定特定的命令获取详细帮助。
     * @return 总是返回true，表示帮助信息已成功发送给玩家。
     */
    private fun onHelp(player: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1) {
            val sb = StringBuilder()
            helpMassage.values.forEach { sb.append(it).append("\n") }
            player.sendMessage("帮助：/book <command>\n$sb")
            return true
        } else {
            helpMassage[p3[1]]?.let { player.sendMessage(it) }
        }
        return true
    }

    /**
     * 过滤字符串中的特定字符。
     *
     * 该函数旨在从字符串中移除一些特殊字符，包括点号(.)、冒号(:)、斜杠(/)和反斜杠(\)。
     * 这对于处理需要去除这些字符的字符串场景非常有用，比如文件路径处理或简单的字符串清洁。
     *
     * @return 返回一个新的字符串，其中不包含点号、冒号、斜杠和反斜杠。
     */
    fun String.filt() = this.replace(".", "").replace(":", "").replace("/", "").replace("\\", "")

}