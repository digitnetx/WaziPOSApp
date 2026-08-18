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

function formatTzs(amount: number) {
  return `TZS ${new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount)}`;
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
      amount: formatTzs(receipt.amount),
    })
  );
}

export function getSunmiStatus() {
  if (typeof window === "undefined" || !window.Sunmi?.getStatus) {
    return "BROWSER";
  }
  return window.Sunmi.getStatus();
}
