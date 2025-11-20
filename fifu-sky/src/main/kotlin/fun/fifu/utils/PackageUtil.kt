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

package `fun`.fifu.utils

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarFile

/**
 * 工具类：包
 * @author NekokeCore
 */
object PackageUtil {
    /**
     * 获取某包下所有类
     * @param packageName 包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    fun getClassName(packageName: String, childPackage: Boolean = true): List<String?>? {
        var fileNames: List<String?>? = null
//        val loader = Thread.currentThread().contextClassLoader
        val loader = this.javaClass.classLoader
        val packagePath = packageName.replace('.', '/').replace('\\', '/')
        val url = loader.getResource(packagePath)
        if (url != null) {
            val type = url.protocol
            if (type == "file") {
                fileNames = getClassNameByFile(url.path, childPackage)
            } else if (type == "jar") {
                fileNames = getClassNameByJar(url.path, childPackage)
            }
        } else {
            fileNames = getClassNameByJars((loader as URLClassLoader).urLs, packagePath, childPackage)
        }
        return fileNames
    }

    /**
     * 从项目文件获取某包下所有类
     * @param filePath 文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    fun getClassNameByFile(filePath: String, childPackage: Boolean): List<String> {
        val myClassName: MutableList<String> = ArrayList()
        val file = File(filePath)
        val childFiles = file.listFiles()
        if (!childFiles.isNullOrEmpty()) for (childFile in childFiles) {
            if (childFile.isDirectory) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.path, childPackage))
                }
            } else {
                var childFilePath = childFile.path
                if (childFilePath.endsWith(".class")) {
                    childFilePath =
                        childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf("."))
                    childFilePath = childFilePath.replace("\\", ".")
                    myClassName.add(childFilePath)
                }
            }
        }
        return myClassName
    }

    /**
     * 从 jar 获取某包下所有类
     * @param jarPath jar 文件路径，示例：D://CraftKotlin.jar!.
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    fun getClassNameByJar(jarPath: String, childPackage: Boolean): List<String?> {
        val myClassName: MutableList<String?> = ArrayList()
        val jarInfo = jarPath.replace('\\', '/').split("!").toTypedArray()
        val indexOf = jarInfo[0].indexOf("/")
        val jarFilePath = jarInfo[0].substring(indexOf)
        val packagePath = jarInfo[1].substring(1)
        try {
            val jarFile = JarFile(jarFilePath)
            val entrys = jarFile.entries()
            while (entrys.hasMoreElements()) {
                val jarEntry = entrys.nextElement()
                var entryName = jarEntry.name
                if (entryName.endsWith(".class")) {
                    if (childPackage) {
                        if (entryName.startsWith(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."))
                            myClassName.add(entryName)
                        }
                    } else {
                        val index = entryName.lastIndexOf("/")
                        val myPackagePath: String = if (index != -1) {
                            entryName.substring(0, index)
                        } else {
                            entryName
                        }
                        if (myPackagePath == packagePath) {
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."))
                            myClassName.add(entryName)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return myClassName
    }

    /**
     * 从所有 jar 中搜索该包，并获取该包下所有类
     * @param urls URL 集合
     * @param packagePath 包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private fun getClassNameByJars(urls: Array<URL>?, packagePath: String, childPackage: Boolean): List<String?> {
        val myClassName: MutableList<String?> = ArrayList()
        if (urls != null) {
            for (i in urls.indices) {
                val url = urls[i]
                val urlPath = url.path
                // 不必搜索 classes 文件夹
                if (urlPath.endsWith("classes/")) {
                    continue
                }
                val jarPath = "$urlPath!/$packagePath"
                myClassName.addAll(getClassNameByJar(jarPath, childPackage))
            }
        }
        return myClassName
    }
}