/*
 * MIT License
 *
 * Copyright (c) 2023 BromineMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.brominemc.oiotung

import org.tinylog.kotlin.Logger
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileReader
import java.net.Socket
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.system.exitProcess

// Constants
const val MAGIC_HEADER: Int = 0xEBACA_BEE.toInt()
const val VERSION: Int = 3
const val ONLINE_MATCH = "%[online]%"
const val MAX_MATCH = "%[max]%"
const val FLASH_MATCH = "%[flash]%"

// Utilities
val RESTART_AFTER: Instant = Instant.now().plus(3, ChronoUnit.DAYS)
val CLIENT: HttpClient = HttpClient.newHttpClient()
var lastWasBoar = false

/**
 * Main function.
 */
fun main() {
    try {
        Logger.info("Starting")

        Logger.info("Loading config")
        val properties = Properties()
        FileReader("oiotung.properties", Charsets.UTF_8).use {
            properties.load(it)
        }
        val config = OConfig(properties)
        Logger.info("Config loaded")

        Logger.info("Oiotung Client by 3fusii for BromineMC")
        Logger.info("GitHub: https://github.com/BromineMC/Oiotung")

        Logger.info("Entering infinite loop")
        while (true) {
            connect(config)
        }
    } catch (e: Throwable) {
        try {
            Logger.error(e, "Error on application start")
        } catch (ex: Throwable) {
            ex.addSuppressed(e)
            e.printStackTrace()
        }
        exitProcess(-1)
    }
}

/**
 * Connects to the host using [config].
 */
fun connect(config: OConfig) {
    try {
        Thread.yield()
        Thread.sleep(config.connectionDelay)

        if (Instant.now().isAfter(RESTART_AFTER)) {
            Logger.info("Time for a scheduled restart. Bye!")
            exitProcess(0)
        }

        Logger.debug("Setting up connection")
        Socket().use { socket ->

            socket.soTimeout = config.serverTimeout
            socket.keepAlive = true
            socket.tcpNoDelay = true
            socket.connect(config.host, config.serverTimeout)

            DataOutputStream(socket.getOutputStream()).use { write ->
                DataInputStream(socket.getInputStream()).use { read ->

                    Logger.debug("Connection set up. Pinging")
                    while (socket.isConnected && socket.isBound && !socket.isClosed) {
                        Thread.yield()
                        Thread.sleep(config.pingDelay)

                        write.writeInt(MAGIC_HEADER)
                        write.writeInt(VERSION)

                        val header = read.readInt()
                        require(header == MAGIC_HEADER) { "Unexpected header: $header" }
                        val version = read.readInt()
                        require(version == VERSION) { "Unexpected version: $version" }

                        val result = OResult(read)
                        Logger.info("Result: $result")

                        var data = config.discordDataOnline
                            .replace(ONLINE_MATCH, result.online.toString(), true)
                            .replace(MAX_MATCH, result.max.toString(), true)
                            .replace(FLASH_MATCH, if (lastWasBoar) ":boar:" else ":pig:", true)
                        for (server in result.servers) {
                            data = data
                                .replace("%[${server.name}_offline_queue]%", server.offlineQueue.toString(), true)
                                .replace("%[${server.name}_deprecated_queue]%", server.deprecatedQueue.toString(), true)
                                .replace("%[${server.name}_online]%", server.players.toString(), true)
                                .replace("%[${server.name}_max]%", server.cap.toString(), true)
                        }
                        Logger.debug("Sending HTTP request...")
                        val response = CLIENT.send(
                            HttpRequest.newBuilder(config.discordEndpoint)
                                .header("User-Agent", "Oiotung/1.0 (BromineMC; abuse: imvidtu@proton.me)")
                                .header("Content-Type", "application/json")
                                .timeout(config.discordTimeout)
                                .method("PATCH", BodyPublishers.ofString(data))
                                .build(), BodyHandlers.ofString()
                        )
                        val fullResponse = "$response (${response.headers()}): ${response.body()}".replace(config.discordEndpoint.toString(), "[ENDPOINT]");
                        Logger.debug("Got HTTP response: $fullResponse")
                        val code = response.statusCode()
                        response.headers().firstValue("Retry-After").ifPresent {
                            Logger.info("HTTP response $fullResponse asked us to sleep for $it, doing it...")
                            Thread.sleep(Duration.ofSeconds(it.toLong()))
                        }
                        require(code in 200..299) { "Invalid response code $code" }
                        lastWasBoar = !lastWasBoar
                    }
                }
            }
        }
    } catch (e: Exception) {
        try {
            val data = config.discordDataOffline.replace(FLASH_MATCH, if (lastWasBoar) ":boar:" else ":pig:", true)
            Logger.debug("Sending HTTP request...")
            val response = CLIENT.send(
                HttpRequest.newBuilder(config.discordEndpoint)
                    .header("User-Agent", "Oiotung/1.0 (BromineMC; abuse: imvidtu@proton.me)")
                    .header("Content-Type", "application/json")
                    .timeout(config.discordTimeout)
                    .method("PATCH", BodyPublishers.ofString(data))
                    .build(), BodyHandlers.ofString()
            )
            val fullResponse = "$response (${response.headers()}): ${response.body()}".replace(config.discordEndpoint.toString(), "[ENDPOINT]");
            Logger.debug("Got HTTP response: $fullResponse")
            val code = response.statusCode()
            response.headers().firstValue("Retry-After").ifPresent {
                Logger.info("HTTP response $fullResponse asked us to sleep for $it, doing it...")
                Thread.sleep(Duration.ofSeconds(it.toLong()))
            }
            require(code in 200..299) { "Invalid response code $code" }
            lastWasBoar = !lastWasBoar
        } catch (ex: Exception) {
            e.addSuppressed(ex)
        }
        Logger.warn(e, "Pinging error")
    }
}