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

    /** 58mm government-bill receipt matched to the supplied reference photograph. */
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

            // Reference receipt uses a normal, non-monospace sans-serif appearance.
            // Explicitly use the printer font API to keep 0 as a plain zero: 0000000000.
            s.setAlignment(1, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            printWithNormalFont(s, "Ministry of Blue Economy and Fisheries\n", 14f)
            printWithNormalFont(s, "\n", 14f)

            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            printWithNormalFont(s, "Government Bill\n", 17f)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            printWithNormalFont(s, "\n", 14f)

            s.setAlignment(0, null)

            // Keep all short fields on one physical line, matching the photograph.
            printLine(s, "BillItem", billItem, 14f)
            printLine(s, "", currencyLine(currency), 14f)
            printLine(s, "Payer name", payerName, 14f)
            printLine(s, "Payer phone", payerPhone, 14f)
            printLine(s, "Amount", "$currency $amount", 14f)
            printLine(s, "Pay option", paymentOption, 14f)
            printLine(s, "Expire Date", compactExpiry(expiryDate), 14f)

            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            printLine(s, "ControlNumber", controlNumber, 14f)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)

            // Instructions start immediately after ControlNumber, with the same wrapping style.
            printWithNormalFont(
                s,
                "Lipa kupitia Benki (NMB/BOT/PBZ) na Mawakala wake au Mitandao ya Simu (kwa\n" +
                "chaguo la \"Malipo ya Serikali\")\n" +
                "Piga namba 0778782798 kwa maelezo zaidi.\n",
                14f
            )

            printLine(s, "POS center", posCenter, 14f)
            printLine(s, "Printed on", printedOn, 14f)
            printLine(s, "Printed By", printedBy, 14f)
            s.lineWrap(2, null)
            true
        } catch (_: RemoteException) { false }
    }

    private fun currencyLine(currency: String): String = "($currency)"

    /** Print with an ordinary sans-serif font to avoid dotted/slashed-zero glyphs. */
    private fun printWithNormalFont(service: SunmiPrinterService, text: String, size: Float) {
        service.printTextWithFont(text, "sans-serif", size, null)
    }

    private fun printLine(service: SunmiPrinterService, label: String, value: String, size: Float) {
        if (label.isEmpty()) {
            printWithNormalFont(service, "$value\n", size)
        } else {
            printWithNormalFont(service, "$label : $value\n", size)
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
