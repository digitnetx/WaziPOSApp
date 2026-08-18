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
            try {
                service.printerInit(null)
            } catch (_: RemoteException) {
            }
        }

        override fun onDisconnected() {
            this@SunmiPrinterManager.service = null
            connected = false
        }
    }

    fun connect(): Boolean {
        return try {
            InnerPrinterManager.getInstance().bindService(context, callback)
        } catch (_: InnerPrinterException) {
            connected = false
            false
        }
    }

    fun disconnect() {
        try {
            InnerPrinterManager.getInstance().unBindService(context, callback)
        } catch (_: InnerPrinterException) {
        } finally {
            service = null
            connected = false
        }
    }

    fun printerStatus(): Int {
        val s = service ?: return 505
        return try {
            s.updatePrinterState()
        } catch (_: RemoteException) {
            505
        }
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
        printedBy: String
    ): Boolean {
        val s = service ?: return false

        return try {
            s.printerInit(null)
            s.setAlignment(1, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            s.printText("${businessName.trim()}\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.printText("Ministry of Blue Economy and Fisheries\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            s.printText("Government Bill\n\n", null)

            s.setAlignment(0, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.printText("BillItem : ${billItem.trim()}\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            s.printText("(TZS)\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.printText("Payer name : $payerName\n", null)
            s.printText("Payer phone : $payerPhone\n", null)
            s.printText("Amount : $amount\n", null)
            s.printText("Pay option : $paymentOption\n", null)
            s.printText("Expire Date : $expiryDate\n", null)
            s.printText("ControlNumber : $controlNumber\n", null)

            s.printText(
                "Lipa kupitia Benki (NMB/BOT/PBZ) na\n" +
                "Mawakala wake au Mitandao ya Simu\n" +
                "(kwa kuchagua \"Malipo ya Serikali\")\n" +
                "Piga namba 0777350786 kwa maelezo\n" +
                "Zaidi.\n\n",
                null
            )

            s.printText("POS center : $posCenter\n", null)
            s.printText("Printed on : $printedOn\n", null)
            s.printText("Printed By : $printedBy\n", null)
            s.lineWrap(4, null)
            true
        } catch (_: RemoteException) {
            false
        }
    }

    fun printTest(): Boolean {
        val s = service ?: return false
        return try {
            s.printerInit(null)
            s.setAlignment(1, null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
            s.printText("WAZI POS\n", null)
            s.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
            s.printText("SUNMI V2S TEST PRINT\n\n", null)
            s.lineWrap(4, null)
            true
        } catch (_: RemoteException) {
            false
        }
    }
}
