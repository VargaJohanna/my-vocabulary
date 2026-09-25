# Preserve Line Numbers for Crashlytics stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve Room Entities, DAOs, and Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Preserve Gson & Retrofit Data Models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Preserve Kotlin Serialization Models
-keep @**.Serializable class * { *; }

# Ignore missing optional annotations
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn org.apache.commons.csv.**
