# Wazi POS SUNMI V2S Integration

## What was changed

Your original Next.js application is preserved.

Added:

- `android/` — native Android wrapper for SUNMI V2S.
- `src/lib/sunmi-bridge.ts` — browser/native bridge helper.
- `/new-receipt` Print Receipt button now calls the native SUNMI printer when available and uses browser printing otherwise.

## Android app URL

The Android wrapper loads:

https://wazi-pos.vercel.app

Change `APP_URL` in:

`android/app/src/main/java/com/wazi/pos/MainActivity.kt`

if you later move Wazi POS to a custom domain.

## SUNMI printer flow

Next.js:

`printReceiptOnSunmi(receipt)`

↓

WebView:

`window.Sunmi.printReceipt(JSON.stringify(...))`

↓

Kotlin:

`SunmiBridge.printReceipt(...)`

↓

SUNMI:

`SunmiPrinterManager.printReceipt(...)`

↓

V2S built-in 58mm thermal printer.

## Build instructions

1. Install Android Studio.
2. Install Android SDK 35 and JDK 17.
3. Open the `android/` folder.
4. Allow Gradle to sync.
5. Connect the SUNMI V2S with USB.
6. Enable Developer Options + USB debugging.
7. Run the application.
8. Go to `/new-receipt`.
9. Generate a bill.
10. Press `Print Receipt`.

## Important testing note

Do not expect `window.print()` to access the V2S internal printer. The native Android bridge is what performs the hardware print.

The normal desktop browser continues to use `window.print()` as a fallback.

## Current receipt mapping

The native printer receives:

- WAZI POS
- Ministry of Blue Economy and Fisheries
- Government Bill
- Bill Item
- Payer name
- Payer phone
- Amount
- Pay option
- Expire Date
- Control Number
- Government payment instructions
- POS center
- Printed on
- Printed by

This mirrors the current `ThermalReceipt.tsx` layout.

## Next improvements

After the first physical print works, the next recommended changes are:

- Exact SUNMI 58mm typography/alignment tuning.
- QR code printing if required.
- Printer status indicator in the Next.js UI.
- Automatic reprint.
- Barcode/scanner bridge if your V2S has the scanner configuration.
- Offline receipt queue.
- Release signing and kiosk/full-screen mode.
