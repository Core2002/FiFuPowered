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
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import java.io.File

object BookOperator {

    /**
     * 复制一本书的元数据，并返回一个新的ItemStack。
     *
     * @return 一个具有相同页面的可写书的ItemStack。
     */
    fun BookMeta.copyBook(): ItemStack {
        val bookMeta = Bukkit.getItemFactory().getItemMeta(Material.WRITABLE_BOOK) as BookMeta
        bookMeta.pages = this.pages
        val book = ItemStack(Material.WRITABLE_BOOK)
        book.itemMeta = bookMeta
        return book
    }

    /**
     * 将书的页面数据导出到指定路径的文件中。
     *
     * @param path 文件的路径。
     */
    fun BookMeta.exportBook(path: String) = File(path).writeText(JSONArray.toJSONString(this.pages))

    /**
     * 从指定路径的文件中导入书的页面数据，并创建相应的书。
     *
     * @param path 文件的路径。
     * @param type 导入后书的材质，默认为可写书。
     * @return 一个具有导入页面的书的ItemStack。
     */
    fun importBook(path: String, type: Material = Material.WRITABLE_BOOK) = makeBook(File(path).readText(), type)

    /**
     * 根据提供的文本和材质类型创建一本书籍物品。
     *
     * @param text 书籍的页面内容，以JSON格式的字符串表示。
     * @param type 书籍的材质类型，默认为可书写的书籍。
     * @return 创建的书籍物品Stack。
     */
    fun makeBook(text: String, type: Material = Material.WRITABLE_BOOK): ItemStack {
        val list = JSONParser().parse(text) as List<String>
        val bookMeta = Bukkit.getItemFactory().getItemMeta(type) as BookMeta
        bookMeta.pages = list
        val book = ItemStack(type)
        if (type == Material.WRITTEN_BOOK) {
            bookMeta.title = "Viewing for $pluginName"
            bookMeta.author = "$pluginName by NekokeCore"
        }
        book.itemMeta = bookMeta
        return book
    }

}