plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.speechandtextrecognitionsystem"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.speechandtextrecognitionsystem"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
//    compileOptions {
//        sourceCompatibility = VERSION_1_8
//        targetCompatibility = VERSION_1_8
//    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.mediarouter:mediarouter:1.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")  //okhtttp库
    implementation("com.squareup.okio:okio:3.2.0")       //okhttp需要依赖的基础

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}