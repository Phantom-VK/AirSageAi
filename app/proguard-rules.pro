# =====================
# Jetpack Compose Rules
# =====================
# Keep Compose classes & avoid internal field removal
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Required to prevent crashes with Compose compiler metadata
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}

# =====================
# Kotlin Coroutines
# =====================
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# =====================
# Moshi / Retrofit
# =====================
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keepclassmembers class * {
    @com.squareup.moshi.JsonClass <methods>;
}
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Keep data models used in Moshi/Retrofit
-keep class com.vikram.airsageai.data.dataclass.** { *; }

# =====================
# Hilt & DI
# =====================
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Needed for Hilt-generated classes
-keep class com.vikram.airsageai.viewmodels.** { *; }
-keep class com.vikram.airsageai.utils.LocationUtils { *; }
-keep class com.vikram.airsageai.data.repository.** { *; }

# =====================
# WorkManager + CoroutineWorker
# =====================
-keep class androidx.work.Worker { *; }
-keep class androidx.work.CoroutineWorker { *; }
-keep class androidx.work.ListenableWorker { *; }

# =====================
# Keep constructors and default members
# =====================
-keepclassmembers class * {
    public <init>(...);
}

# =====================
# Optional but useful
# =====================
# If you want readable stack traces
#-keepattributes SourceFile,LineNumberTable

# If you want to hide original file names
#-renamesourcefileattribute SourceFile
