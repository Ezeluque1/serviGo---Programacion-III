// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // KSP: genera el codigo de Room en tiempo de compilacion.
    alias(libs.plugins.ksp) apply false
}
