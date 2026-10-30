plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.viceos.tv"
    compileSdk = 35
    defaultConfig {
        // Keep existing package ID so Android can update VICE OS without uninstalling it.
        applicationId = "com.viceos.tv"
        minSdk = 23
        targetSdk = 28
        versionCode = 2
        versionName = "1.0-calian"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
