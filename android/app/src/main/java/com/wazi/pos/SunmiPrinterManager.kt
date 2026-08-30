package com.wazi.pos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.RemoteException
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService

class SunmiPrinterManager(private val context: Context) {
    @Volatile private var service: SunmiPrinterService? = null
    @Volatile var connected: Boolean = false
        private set

    private val callback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            this@SunmiPrinterManager.service = service
            connected = true
            try { service.printerInit(null) } catch (_: RemoteException) { }
        }
        override fun onDisconnected() {
            this@SunmiPrinterManager.service = null
            connected = false
        }
    }

    fun connect(): Boolean {
        if (connected) return true
        return try { InnerPrinterManager.getInstance().bindService(context.applicationContext, callback) }
        catch (_: InnerPrinterException) { connected = false; false }
    }

    fun disconnect() {
        try { InnerPrinterManager.getInstance().unBindService(context.applicationContext, callback) }
        catch (_: InnerPrinterException) { }
        finally { service = null; connected = false }
    }

    fun printerStatus(): Int {
        val s = service ?: return 505
        return try { s.updatePrinterState() } catch (_: RemoteException) { 505 }
    }

    fun printReceipt(
        businessName: String,
        receiptNumber: String,
        billItem: String,
        payerName: String,
        payerPhone: String,
        amount: String,
        paymentOption: String,
        expiryDate: String,
        controlNumber: String,
        posCenter: String,
        printedOn: String,
        printedBy: String,
        currency: String = "TZS"
    ): Boolean {
        val s = service ?: return false
        return try {
            s.printerInit(null)
            val bitmap = buildReceiptBitmap(
                billItem, payerName, payerPhone, amount, paymentOption,
                compactExpiry(expiryDate), controlNumber, posCenter, printedOn,
                printedBy, currency
            )
            s.printBitmap(bitmap, null)
            s.lineWrap(2, null)
            bitmap.recycle()
            true
        } catch (_: Exception) { false }
    }

    private data class ReceiptBlock(
        val text: String,
        val size: Float = 20f,
        val bold: Boolean = false,
        val center: Boolean = false,
        val gapAfter: Int = 0,
        val condensed: Boolean = false,
        val noWrap: Boolean = false
    )

    private fun buildReceiptBitmap(
        billItem: String,
        payerName: String,
        payerPhone: String,
        amount: String,
        paymentOption: String,
        expiryDate: String,
        controlNumber: String,
        posCenter: String,
        printedOn: String,
        printedBy: String,
        currency: String
    ): Bitmap {
        val width = 384
        val side = 14
        val contentWidth = width - (side * 2)

        val blocks = listOf(
            // Keep the complete ministry name on one line, like the original.
            ReceiptBlock(
                "Ministry of Blue Economy and Fisheries",
                17f,
                bold = true,
                center = true,
                gapAfter = 20,
                condensed = true,
                noWrap = true
            ),
            ReceiptBlock(
                "Government Bill",
                19f,
                bold = true,
                center = true,
                gapAfter = 20
            ),
            // Keep the complete BillItem on one line.
            ReceiptBlock(
                "BillItem : $billItem",
                14f,
                gapAfter = 0,
                condensed = true,
                noWrap = true
            ),
            // No vertical gap between BillItem, currency and payer name.
            ReceiptBlock("($currency)", 20f, gapAfter = 0),
            ReceiptBlock("Payer name : $payerName", 20f, gapAfter = 0),
            ReceiptBlock("Payer phone : $payerPhone", 20f, gapAfter = 0),
            ReceiptBlock("Amount : $currency $amount", 20f, gapAfter = 0),
            ReceiptBlock("Pay option : $paymentOption", 20f, gapAfter = 0),
            ReceiptBlock("Expire Date : $expiryDate", 20f, gapAfter = 0),
            // No gap after ControlNumber: payment instructions start immediately.
            ReceiptBlock("ControlNumber : $controlNumber", 20f, bold = true, gapAfter = 0),
            ReceiptBlock(
                "Lipa kupitia Benki (NMB/BOT/PBZ) na Mawakala wake au Mitandao ya Simu (kwa\n" +
                    "kuchagua \"Malipo ya Serikali\")\n" +
                    "Piga namba 0778782798 kwa maelezo zaidi.",
                20f,
                gapAfter = 30
            ),
            // Add a clear gap between payment instructions and footer, matching the original.
            ReceiptBlock("POS center : $posCenter", 20f, gapAfter = 0),
            ReceiptBlock("Printed on : ${formatPrintedOn(printedOn)}", 20f, gapAfter = 0),
            ReceiptBlock("Printed By : $printedBy", 20f)
        )

        var requiredHeight = 20
        val layouts = ArrayList<Pair<ReceiptBlock, StaticLayout>>()
        for (block in blocks) {
            val paint = textPaint(block.size, block.bold, block.condensed)
            val layoutWidth = if (block.noWrap) {
                maxOf(contentWidth, kotlin.math.ceil(paint.measureText(block.text)).toInt() + 2)
            } else {
                contentWidth
            }
            val layout = StaticLayout.Builder
                .obtain(block.text, 0, block.text.length, paint, layoutWidth)
                .setAlignment(if (block.center) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL)
                .setIncludePad(false)
                .setLineSpacing(0f, 1.0f)
                .build()
            layouts += block to layout
            requiredHeight += layout.height + block.gapAfter
        }

        val bitmap = Bitmap.createBitmap(width, requiredHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        val canvas = Canvas(bitmap)
        var y = 10f

        for ((block, layout) in layouts) {
            val drawWidth = layout.width
            val x = when {
                block.center -> ((width - drawWidth) / 2f).coerceAtLeast(0f)
                block.noWrap && drawWidth > contentWidth -> side.toFloat()
                else -> side.toFloat()
            }
            canvas.save()
            canvas.translate(x, y)
            layout.draw(canvas)
            canvas.restore()
            y += layout.height + block.gapAfter
        }
        return bitmap
    }

    private fun textPaint(size: Float, bold: Boolean, condensed: Boolean = false): TextPaint {
        return TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = size
            val family = if (condensed) "sans-serif-condensed" else "sans-serif"
            typeface = Typeface.create(family, if (bold) Typeface.BOLD else Typeface.NORMAL)
            fontFeatureSettings = "-zero"
            isDither = true
        }
    }

    private fun formatPrintedOn(value: String): String {
        val trimmed = value.trim()
        if (trimmed.contains('T')) return trimmed
        val parts = trimmed.split(Regex("\\s+"))
        return if (parts.size >= 2) "${parts[0]}T${parts[1]}" else trimmed
    }

    private fun compactExpiry(value: String): String {
        val parts = value.trim().split(Regex("\\s+"))
        if (parts.size < 2) return value.trim()
        return parts[0] + " " + parts[1].replace(":", "")
    }

    fun printTest(): Boolean {
        val s = service ?: return false
        return try {
            s.printerInit(null)
            val bitmap = Bitmap.createBitmap(384, 90, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            val canvas = Canvas(bitmap)
            val paint = textPaint(24f, false)
            val text = "0000000000"
            val x = (384f - paint.measureText(text)) / 2f
            canvas.drawText(text, x, 32f, paint)
            val normal = textPaint(16f, false)
            canvas.drawText("WAZI POS - PLAIN ZERO TEST", 52f, 64f, normal)
            s.printBitmap(bitmap, null)
            s.lineWrap(2, null)
            bitmap.recycle()
            true
        } catch (_: Exception) { false }
    }
}
