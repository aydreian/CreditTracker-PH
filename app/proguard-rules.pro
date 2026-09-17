# Keep Groq Network Models and DAOs
-keep class com.example.credittrackph.data.network.** { *; }
-keepclassmembers class com.example.credittrackph.data.network.** { *; }
-keep class com.example.credittrackph.data.db.entity.** { *; }
-keepclassmembers class com.example.credittrackph.data.db.entity.** { *; }

# Keep Gson SerializedName fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Retrofit & OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Retain generic signatures and annotations
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

