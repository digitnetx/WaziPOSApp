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

    /** 58mm government-bill layout matched to the supplied original receipt. */
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

            // The original receipt is a simple 58mm thermal print. Keep the
            // font plain and compact so long government text fits naturally.
            s.setFontSize(14f, null)
            s.setAlignment(1, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.printText("Ministry of Blue Economy and Fisheries\n", null)

            // Deliberate vertical gap between ministry name and Government Bill.
            s.printText("\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            s.printText("Government Bill\n", null)
            s.printText("\n\n", null)

            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.setAlignment(0, null)
            printLine(s, "BillItem", billItem)
            s.printText("($currency)\n", null)
            s.printText("\n", null)

            printLine(s, "Payer name", payerName)
            printLine(s, "Payer phone", payerPhone)
            printLine(s, "Amount", "$currency $amount")
            printLine(s, "Pay option", paymentOption)
            printLine(s, "Expire Date", compactExpiry(expiryDate))

            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            printLine(s, "ControlNumber", controlNumber)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)

            s.printText(
                "\nLipa kupitia Benki (NMB/BOT/PBZ) na\n" +
                "Mawakala wake au Mitandao ya Simu\n" +
                "(kwa kuchagua \"Malipo ya Serikali\")\n" +
                "Piga namba 0777350786 kwa maelezo Zaidi.\n\n",
                null
            )

            printLine(s, "POS center", posCenter)
            printLine(s, "Printed on", printedOn)
            printLine(s, "Printed By", printedBy)
            s.lineWrap(2, null)
            true
        } catch (_: RemoteException) { false }
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
            s.setFontSize(14f, null)
            s.setAlignment(1, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            s.printText("WAZI POS\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.printText("SUNMI V2S TEST PRINT\n\n", null)
            s.lineWrap(2, null)
            true
        } catch (_: RemoteException) { false }
    }
}
