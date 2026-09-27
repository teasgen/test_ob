// Root build.gradle.kts
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false  // ← Обновлено с 1.9.0 на 1.9.20
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}