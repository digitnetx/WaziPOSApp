package com.wazi.pos

import android.content.Context
import android.os.RemoteException
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import com.sunmi.peripheral.printer.WoyouConsts

class SunmiPrinterManager(private val context: Context) {
    @Volatile
    private var service: SunmiPrinterService? = null

    @Volatile
    var connected: Boolean = false
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
        return try {
            InnerPrinterManager.getInstance().bindService(context.applicationContext, callback)
        } catch (_: InnerPrinterException) {
            connected = false
            false
        }
    }

    fun disconnect() {
        try {
            InnerPrinterManager.getInstance().unBindService(context.applicationContext, callback)
        } catch (_: InnerPrinterException) {
        } finally {
            service = null
            connected = false
        }
    }

    fun printerStatus(): Int {
        val s = service ?: return 505
        return try { s.updatePrinterState() } catch (_: RemoteException) { 505 }
    }

    /**
     * Compact government-bill print layout matching the supplied short 58mm
     * reference. The receipt is intentionally text-only so it stays crisp on
     * the built-in SUNMI thermal printer.
     */
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
        printedBy: String
    ): Boolean {
        val s = service ?: return false

        return try {
            s.printerInit(null)

            // Small thermal font and tight spacing reproduce the short reference.
            s.setFontSize(20f, null)
            s.setAlignment(1, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.printText("Ministry of Blue Economy and Fisheries\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            s.printText("Government Bill\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)

            s.setAlignment(0, null)
            printLine(s, "BillItem", billItem)
            s.printText("(TZS)\n", null)
            printLine(s, "Payer name", payerName)
            printLine(s, "Payer phone", payerPhone)
            printLine(s, "Amount", amount)
            printLine(s, "Pay option", paymentOption)
            printLine(s, "Expire Date", compactExpiry(expiryDate))

            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            printLine(s, "ControlNumber", controlNumber)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)

            s.printText(
                "\nLipa kupitia Benki (NMB/BOT/PBZ) na\n" +
                "Mawakala wake au Mitandao ya Simu\n" +
                "(kwa kuchagua \"Malipo ya Serikali\")\n" +
                "Piga namba 0777350786 kwa maelezo Zaidi.\n",
                null
            )

            printLine(s, "POS center", posCenter)
            printLine(s, "Printed on", printedOn)
            printLine(s, "Printed By", printedBy)
            s.lineWrap(3, null)
            true
        } catch (_: RemoteException) {
            false
        }
    }

    private fun printLine(service: SunmiPrinterService, label: String, value: String) {
        service.printText("$label : $value\n", null)
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
            s.setFontSize(20f, null)
            s.setAlignment(1, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            s.printText("WAZI POS\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.printText("SUNMI V2S TEST PRINT\n\n", null)
            s.lineWrap(3, null)
            true
        } catch (_: RemoteException) {
            false
        }
    }
}
