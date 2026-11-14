package com.bestid.core.data.backup

import kotlinx.datetime.LocalDateTime
import okio.BufferedSink

/**
 * Escritor de zip mínimo, sin compresión (método "stored"): las fotos ya son JPEG y el JSON pesa
 * poco. okio solo lee zips, por eso se escribe aquí. Sin zip64: menos de 4 GB y 65 535 archivos.
 */
internal class ZipWriter(private val sink: BufferedSink, modified: LocalDateTime) {
    private class Entry(val name: ByteArray, val crc: Int, val size: Int, val offset: Long)

    private val entries = mutableListOf<Entry>()
    private var offset = 0L
    private val dosTime = (modified.hour shl 11) or (modified.minute shl 5) or (modified.second / 2)
    private val dosDate = ((modified.year - 1980).coerceAtLeast(0) shl 9) or (modified.month.ordinal + 1 shl 5) or modified.day

    fun add(name: String, bytes: ByteArray) {
        val nameBytes = name.encodeToByteArray()
        val crc = Crc32.of(bytes)
        entries += Entry(nameBytes, crc, bytes.size, offset)
        sink.writeIntLe(0x04034b50)
        sink.writeShortLe(VERSION)
        sink.writeShortLe(UTF8_FLAG)
        sink.writeShortLe(0) // stored
        sink.writeShortLe(dosTime)
        sink.writeShortLe(dosDate)
        sink.writeIntLe(crc)
        sink.writeIntLe(bytes.size)
        sink.writeIntLe(bytes.size)
        sink.writeShortLe(nameBytes.size)
        sink.writeShortLe(0)
        sink.write(nameBytes)
        sink.write(bytes)
        offset += 30 + nameBytes.size + bytes.size
    }

    fun finish() {
        val start = offset
        for (e in entries) {
            sink.writeIntLe(0x02014b50)
            sink.writeShortLe(VERSION)
            sink.writeShortLe(VERSION)
            sink.writeShortLe(UTF8_FLAG)
            sink.writeShortLe(0)
            sink.writeShortLe(dosTime)
            sink.writeShortLe(dosDate)
            sink.writeIntLe(e.crc)
            sink.writeIntLe(e.size)
            sink.writeIntLe(e.size)
            sink.writeShortLe(e.name.size)
            sink.writeShortLe(0) // extra
            sink.writeShortLe(0) // comentario
            sink.writeShortLe(0) // disco
            sink.writeShortLe(0) // atributos internos
            sink.writeIntLe(0) // atributos externos
            sink.writeIntLe(e.offset.toInt())
            sink.write(e.name)
            offset += 46 + e.name.size
        }
        sink.writeIntLe(0x06054b50)
        sink.writeShortLe(0)
        sink.writeShortLe(0)
        sink.writeShortLe(entries.size)
        sink.writeShortLe(entries.size)
        sink.writeIntLe((offset - start).toInt())
        sink.writeIntLe(start.toInt())
        sink.writeShortLe(0)
        sink.flush()
    }

    private companion object {
        const val VERSION = 20
        const val UTF8_FLAG = 0x0800
    }
}

/** CRC-32 (IEEE 802.3), el que pide el formato zip. */
internal object Crc32 {
    private val table = IntArray(256) { n ->
        var c = n
        repeat(8) { c = if (c and 1 != 0) (c ushr 1) xor 0xEDB88320.toInt() else c ushr 1 }
        c
    }

    fun of(bytes: ByteArray): Int {
        var crc = -1
        for (b in bytes) crc = table[(crc xor b.toInt()) and 0xFF] xor (crc ushr 8)
        return crc.inv()
    }
}
