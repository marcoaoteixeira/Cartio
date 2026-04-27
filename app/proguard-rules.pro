# Preserve line numbers in stack traces for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room: keep entity and DAO class names (used via reflection by Room's generated code)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Hilt: keep generated component and module classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }

# Kotlin: keep coroutine internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin Metadata: required for reflection-based libraries (Hilt, Room)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Keep Kotlin data classes used in Room entities (prevent field name obfuscation)
-keepclassmembers class com.nameless.cartio.core.database.entity.** { *; }
