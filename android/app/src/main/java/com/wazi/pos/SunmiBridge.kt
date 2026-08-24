package com.wazi.pos

import android.webkit.JavascriptInterface
import org.json.JSONObject

class SunmiBridge(private val printer: SunmiPrinterManager) {

    @JavascriptInterface
    fun isAvailable(): Boolean = printer.connected

    @JavascriptInterface
    fun getStatus(): String {
        return when (printer.printerStatus()) {
            1 -> "RUNNING"
            2 -> "INITIALIZING"
            3 -> "HARDWARE_ERROR"
            4 -> "OUT_OF_PAPER"
            5 -> "OVERHEATING"
            6 -> "COVER_OPEN"
            7 -> "CUTTER_ERROR"
            8 -> "CUTTER_OK"
            505 -> "PRINTER_NOT_FOUND"
            else -> "UNKNOWN"
        }
    }

    @JavascriptInterface
    fun testPrint(): Boolean = printer.printTest()

    @JavascriptInterface
    fun printReceipt(receiptJson: String): Boolean {
        return try {
            val r = JSONObject(receiptJson)
            printer.printReceipt(
                businessName = r.optString("businessName", "WAZI POS"),
                receiptNumber = r.optString("receiptNumber"),
                billItem = r.optString("billItem"),
                payerName = r.optString("customerName"),
                payerPhone = r.optString("customerPhone"),
                amount = r.optString("amount"),
                paymentOption = r.optString("paymentOption"),
                expiryDate = r.optString("expiryDate"),
                controlNumber = r.optString("controlNumber"),
                posCenter = r.optString("posCenterName"),
                printedOn = r.optString("printedAt"),
                printedBy = r.optString("printedBy"),
                currency = r.optString("currency", "TZS")
            )
        } catch (_: Exception) {
            false
        }
    }
}
