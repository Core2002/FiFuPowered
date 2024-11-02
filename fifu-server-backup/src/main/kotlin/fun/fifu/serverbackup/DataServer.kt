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

import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.time.SystemTimeProvider
import `fun`.fifu.serverbackup.BackupManager.configPojo
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import java.nio.file.Paths

object DataServer {
    private val vertx = Vertx.vertx()
    private val timeProvider = SystemTimeProvider()
    private val codeGenerator = DefaultCodeGenerator(HashingAlgorithm.SHA512)
    private val verifier = DefaultCodeVerifier(codeGenerator, timeProvider).apply {
        setAllowedTimePeriodDiscrepancy(3)
    }

    /**
     * Starts an HTTP server and sets the size limit for uploaded files.
     *
     * @param port The port number the server listens on.
     * @param limitBytes The maximum allowed size of uploaded files (in bytes).
     */
    fun startHTTPServer(port: Int, limitBytes: Long) {
        val server = vertx.createHttpServer(
            HttpServerOptions()
        )

        val router = Router.router(vertx)

        val bodyHandler = BodyHandler.create()
            .setUploadsDirectory("uploads")
            .setBodyLimit(limitBytes)

        // Enable body handling with file uploads going to a temporary directory
        router.route().handler(bodyHandler)

        router.post("/upload")
            .handler(this::validateCode)
            .handler(this::uploadFile)

        server.requestHandler(router).listen(port) { http ->
            if (http.succeeded()) {
                println("HTTP server started on port ${http.result().actualPort()}")
            } else {
                println("HTTP server failed to start")
            }
        }
    }

    private fun handleFileDeletion(routingContext: RoutingContext) {
        routingContext.cancelAndCleanupFileUploads()
        val clientIp = routingContext.request().remoteAddress().host()
        val fileUploads = routingContext.fileUploads()
        for (fileUpload in fileUploads) {
            val uploadedFileName = fileUpload.uploadedFileName()
            println("An unverified attacker has been intercepted. The upload of the file <$uploadedFileName> has been canceled. Their IP address is <$clientIp>.")
            vertx.fileSystem().delete(uploadedFileName)
        }
    }

    /**
     * Handles file upload requests.
     *
     * This function first checks if there are any file uploads in the RoutingContext.
     * If not, it ends the response with a 400 status code and an error message.
     * If files are uploaded, it iterates through each file, logs the file name and size,
     * and moves the uploaded file to a permanent storage location.
     * If the file movement is successful, it ends the response with a success message.
     * If the file movement fails, it ends the response with a 200 status code and a failure message.
     *
     * @param routingContext The context of the routing request, containing the uploaded file information.
     */
    private fun uploadFile(routingContext: RoutingContext) {
        val fileUploads = routingContext.fileUploads()
        if (fileUploads.isEmpty()) {
            routingContext.response().setStatusCode(400).end("No file uploaded")
            return
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


    /**
     * Validates the client's access authorization.
     *
     * This function checks if the client has provided a valid authorization code. If not, it returns an unauthorized error.
     * If the authorization is valid, it allows the request to proceed to the next handler.
     *
     * @param routingContext The Vert.x routing context containing the request and response objects, among others.
     */
    private fun validateCode(routingContext: RoutingContext) {
        // Check if the user has the required permissions to access this resource.
        val codeForClient = routingContext.request().getHeader("Authorization")
        try {
            if (!verifier.isValidCode(configPojo.sendRemoteServerSecret, codeForClient)) {
                handleFileDeletion(routingContext)
                routingContext.response().setStatusCode(401).end("Unauthorized")
                return
            }
        } catch (e: Exception) {
            handleFileDeletion(routingContext)
            routingContext.response().setStatusCode(402).end("Unauthorized, Has exception in ValidCode")
            return
        }

        // If the code is valid, proceed to the next handler in the chain.
        routingContext.next()
    }
}