package com.wazi.pos

import android.content.Context
import android.os.RemoteException
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import com.sunmi.peripheral.printer.WoyouConsts

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

    /** 58mm government-bill layout. Uses the printer's font-rendering API with a normal sans-serif typeface. */
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

            // Use an explicit normal sans-serif typeface so the printer does not use
            // a dotted/slashed-zero variant. The zero must look exactly like: 0.
            s.setFontSize(16f, null)
            s.setAlignment(1, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            printWithNormalFont(s, "Ministry of Blue Economy and Fisheries\n", 16f)
            printWithNormalFont(s, "\n", 16f)
            printWithNormalFont(s, "Government Bill\n", 16f)
            printWithNormalFont(s, "\n", 16f)

            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.setAlignment(0, null)
            s.setFontSize(14f, null)

            printLine(s, "BillItem", billItem)
            printLine(s, "", currencyLine(currency))
            printLine(s, "Payer name", payerName)
            printLine(s, "Payer phone", payerPhone)
            printLine(s, "Amount", "$currency $amount")
            printLine(s, "Pay option", paymentOption)
            printLine(s, "Expire Date", compactExpiry(expiryDate))

            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            printLine(s, "ControlNumber", controlNumber)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)

            printWithNormalFont(
                s,
                "Lipa kupitia Benki (NMB/BOT/PBZ) na Mawakala wake au Mitandao ya Simu (kwa\n" +
                "kuchagua \"Malipo ya Serikali\")\n" +
                "Piga namba 0778782798 kwa maelezo zaidi.\n",
                14f
            )

            printLine(s, "POS center", posCenter)
            printLine(s, "Printed on", printedOn)
            printLine(s, "Printed By", printedBy)
            s.lineWrap(2, null)
            true
        } catch (_: RemoteException) { false }
    }

    private fun currencyLine(currency: String): String = "($currency)"

    /** Print using a normal sans-serif typeface; avoids special dotted/slashed zero glyphs. */
    private fun printWithNormalFont(service: SunmiPrinterService, text: String, size: Float) {
        service.printTextWithFont(text, "sans-serif", size, null)
    }

    private fun printLine(service: SunmiPrinterService, label: String, value: String) {
        if (label.isEmpty()) {
            printWithNormalFont(service, "$value\n", 14f)
        } else {
            printWithNormalFont(service, "$label : $value\n", 14f)
        }
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
            s.setFontSize(16f, null)
            s.setAlignment(1, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            printWithNormalFont(s, "WAZI POS\n", 16f)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            printWithNormalFont(s, "SUNMI V2S TEST PRINT\n\n", 16f)
            s.lineWrap(2, null)
            true
        } catch (_: RemoteException) { false }
    }
}
