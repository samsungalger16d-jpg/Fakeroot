plugins {
    id("com.android.application")
}

android {
    namespace = "com.fakeroot.sim"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fakeroot.sim"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
}
