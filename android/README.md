# Wazi POS — SUNMI V2S Android Wrapper

This folder is a native Android wrapper around the existing Wazi POS Next.js app.

## What it does

- Opens https://wazi-pos.vercel.app in a WebView.
- Exposes a JavaScript bridge named `window.Sunmi`.
- Uses the official SUNMI printer library to access the built-in V2S printer.
- Provides:
  - `window.Sunmi.isAvailable()`
  - `window.Sunmi.getStatus()`
  - `window.Sunmi.testPrint()`
  - `window.Sunmi.printReceipt(JSON.stringify(receipt))`

## Requirements

- Android Studio
- Android SDK 35
- JDK 17
- A SUNMI V2S for printer testing
- Internet connection for the first Gradle sync

## SUNMI dependency

The project uses:

    implementation("com.sunmi:printerlibrary:1.0.18")

SUNMI's official printer demo currently documents this dependency.

## Build

Open the `android/` folder in Android Studio.

Then:

1. Wait for Gradle sync.
2. Connect the SUNMI V2S by USB.
3. Enable Developer Options and USB debugging on the V2S.
4. Press Run in Android Studio.

Or generate a debug APK:

    gradlew.bat assembleDebug

APK output:

    app/build/outputs/apk/debug/app-debug.apk

## First hardware test

After installation, the app opens Wazi POS.

The Next.js integration includes a native-print helper. If you want to verify the hardware before editing the POS screen, open Chrome remote debugging or temporarily call:

    window.Sunmi.testPrint()

A successful result should print a small Wazi POS test receipt.

## Next.js integration

A helper was added at:

    ../src/lib/sunmi-bridge.ts

The `/new-receipt` page was changed so the Print Receipt button:

1. Tries `window.Sunmi.printReceipt(...)` when running inside the SUNMI Android wrapper.
2. Falls back to `window.print()` in a normal browser.

## Important

The Android project is source-ready, but the final APK must be built on your machine with Android Studio/Gradle because the SUNMI library and Android SDK need to be resolved in the Android build environment.

If Gradle reports a SUNMI library/API mismatch, use the current version shown in SUNMI's official developer documentation and update the one dependency line in `app/build.gradle.kts`.
