# --- Apache POI Rules ---
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.slf4j.**

# Keep Java 8 MethodHandle support
-keepclassmembers class * {
    java.lang.invoke.MethodHandle *;
}