plugins {
    id("com.android.application") version "8.2.2" apply false

    // ✅ Kotlin (correct)
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false

    // ✅ KSP MUST MATCH Kotlin
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}