# Jakarta Mail
-keep class com.sun.mail.** { *; }
-keep class jakarta.mail.** { *; }
-keep class jakarta.activation.** { *; }
-dontwarn com.sun.mail.**
-dontwarn jakarta.mail.**
-dontwarn jakarta.activation.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.fdtracker.**$$serializer { *; }
-keepclassmembers class com.fdtracker.** {
    *** Companion;
}
-keepclasseswithmembers class com.fdtracker.** {
    kotlinx.serialization.KSerializer serializer(...);
}
