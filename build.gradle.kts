// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    /*
    added plugins
     */
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("androidx.room") version "2.6.1" apply false
}
