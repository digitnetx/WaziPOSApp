"use client";

import type { Receipt } from "./types";

declare global {
  interface Window {
    Sunmi?: {
      isAvailable?: () => boolean;
      getStatus?: () => string;
      testPrint?: () => boolean;
      printReceipt?: (receiptJson: string) => boolean;
    };
  }
}

export function isSunmiApp() {
  return typeof window !== "undefined" && !!window.Sunmi;
}

export function printReceiptOnSunmi(receipt: Receipt) {
  if (typeof window === "undefined" || !window.Sunmi?.printReceipt) {
    return false;
  }

  return window.Sunmi.printReceipt(
    JSON.stringify({
      ...receipt,
      businessName: "WAZI POS",
      receiptNumber: receipt.controlNumber,
      // Keep the numeric amount unchanged. The native printer applies the
      // selected TZS/USD currency so USD is never incorrectly printed as TZS.
      amount: String(receipt.amount),
    })
  );
}

export function getSunmiStatus() {
  if (typeof window === "undefined" || !window.Sunmi?.getStatus) {
    return "BROWSER";
  }
  return window.Sunmi.getStatus();
}
