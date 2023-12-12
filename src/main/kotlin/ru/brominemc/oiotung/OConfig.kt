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

import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.URI
import java.time.Duration
import java.util.Properties

/**
 * Oiotung configuration.
 * @property host Server host
 * @property serverTimeout Server connection timeout
 * @property connectionDelay Server connection delay
 * @property pingDelay Server ping delay
 * @property discordEndpoint Discord endpoint
 * @property discordTimeout Discord request timeout
 * @property discordDataOnline Discord request online data
 * @property discordDataOffline Discord request offline data
 * @author threefusii
 */
data class OConfig(val host: SocketAddress, val serverTimeout: Int, val connectionDelay: Duration,
                   val pingDelay: Duration, val discordEndpoint: URI, val discordTimeout: Duration,
                   val discordDataOnline: String, val discordDataOffline: String) {
    constructor(properties: Properties) : this(
        InetSocketAddress(
            requireNotNull(properties.getProperty("host")) { "CONFIG - host is null" },
            requireNotNull(properties.getProperty("port")) { "CONFIG - port is null" }.toInt()
        ),
        requireNotNull(properties.getProperty("serverTimeout")) { "CONFIG - serverTimeout is null" }.toInt(),
        Duration.ofMillis(requireNotNull(properties.getProperty("connectionDelay")) { "CONFIG - connectionDelay is null" }.toLong()),
        Duration.ofMillis(requireNotNull(properties.getProperty("pingDelay")) { "CONFIG - pingDelay is null" }.toLong()),
        URI(requireNotNull(properties.getProperty("discordEndpoint")) { "CONFIG - discordEndpoint is null" }),
        Duration.ofMillis(requireNotNull(properties.getProperty("discordTimeout")) { "CONFIG - discordTimeout is null" }.toLong()),
        requireNotNull(properties.getProperty("discordDataOnline")) { "CONFIG - discordDataOnline is null" },
        requireNotNull(properties.getProperty("discordDataOffline")) { "CONFIG - discordDataOffline is null" }
    )
}
