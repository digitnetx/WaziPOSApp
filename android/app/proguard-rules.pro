# SUNMI printer service/library
-keep class com.sunmi.** { *; }
-keep class woyou.** { *; }

# Required by some SUNMI printer library builds when R8 is enabled.
-keep class android.os.SystemProperties { *; }
