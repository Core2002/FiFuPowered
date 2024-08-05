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

package `fun`.fifu.serverbackup

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import java.nio.file.Paths

object DataServer {
    /**
     * Starts an HTTP server and sets the size limit for uploaded files.
     *
     * @param port The port number the server listens on.
     * @param limitBytes The maximum allowed size of uploaded files (in bytes).
     */
    fun startHTTPServer(port: Int, limitBytes: Long) {
        val vertx = Vertx.vertx()
        val server = vertx.createHttpServer(
            HttpServerOptions()
        )

        val router = Router.router(vertx)

        val bodyHandler = BodyHandler.create()
            .setUploadsDirectory("uploads")
            .setBodyLimit(limitBytes)

        // Enable body handling with file uploads going to a temporary directory
        router.route().handler(bodyHandler)

        router.post("/upload").handler { routingContext ->
            val fileUploads = routingContext.fileUploads()
            if (fileUploads.isEmpty()) {
                routingContext.response().setStatusCode(400).end("No file uploaded")
                return@handler
            }

            for (fileUpload in fileUploads) {
                val uploadedFileName = fileUpload.uploadedFileName()
                val fileName = fileUpload.fileName()
                val fileSize = fileUpload.size()

                println("Received file: $fileName (size: $fileSize bytes)")

                // Move the file to a permanent location if needed
                vertx.fileSystem().move(uploadedFileName, Paths.get("uploads", fileName).toString()) {
                    if (it.succeeded()) {
                        routingContext.response().end("File uploaded successfully")
                    } else {
                        routingContext.response().setStatusCode(200).end("Ok, but failed to move uploaded file")
                    }
                }
            }
        }

        server.requestHandler(router).listen(port) { http ->
            if (http.succeeded()) {
                println("HTTP server started on port ${http.result().actualPort()}")
            } else {
                println("HTTP server failed to start")
            }
        }
    }
}