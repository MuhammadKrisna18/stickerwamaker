-keepattributes *Annotation*,InnerClasses,Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * extends kotlinx.serialization.internal.GeneratedSerializer {
    <fields>;
    <methods>;
}
-keepclassmembers class **$Companion {
    public kotlinx.serialization.KSerializer serializer();
}

-keep class com.krisna.stickerwamaker.data.model.** { *; }
-keep class com.krisna.stickerwamaker.updater.** { *; }
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class androidx.navigation3.** { *; }

