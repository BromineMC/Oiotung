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

import java.io.DataInputStream

/**
 * Oiotung result server.
 * @property name Server name
 * @property offlineQueue Players in the offline queue
 * @property deprecatedQueue Players in the deprecated queue
 * @property players Players on the server
 * @property cap Maximum server online
 * @author threefusii
 */
data class OServer(val name: String, val offlineQueue: Int, val deprecatedQueue: Int, val players: Int, val cap: Int) {
    /**
     * Reads the server from the [input].
     */
    constructor(input: DataInputStream) : this(
        String(input.readNBytes(input.readUnsignedByte()), Charsets.UTF_8), // Name
        input.readUnsignedShort(), // Offline Queue
        input.readUnsignedShort(), // Deprecated Queue
        input.readUnsignedShort(), // Players
        input.readUnsignedShort() // Cap
    )
}
