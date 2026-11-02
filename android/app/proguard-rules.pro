# Rucart ProGuard Rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.rucart.app.model.** { *; }
-keep class com.rucart.app.api.** { *; }
