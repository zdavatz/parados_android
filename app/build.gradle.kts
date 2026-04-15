import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ywesee.parados"
    compileSdk = 35

    signingConfigs {
        create("release")
    }

    defaultConfig {
        applicationId = "com.ywesee.parados"
        minSdk = 24
        targetSdk = 35
        versionCode = 11
        versionName = "1.8"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val propFile = file("../signing.properties")
if (propFile.canRead()) {
    val props = Properties()
    props.load(FileInputStream(propFile))
    if (props.containsKey("STORE_FILE") &&
        props.containsKey("STORE_PASSWORD") &&
        props.containsKey("KEY_ALIAS") &&
        props.containsKey("KEY_PASSWORD")) {
        android.signingConfigs.getByName("release").apply {
            storeFile = file(props["STORE_FILE"] as String)
            storePassword = props["STORE_PASSWORD"] as String
            keyAlias = props["KEY_ALIAS"] as String
            keyPassword = props["KEY_PASSWORD"] as String
        }
    } else {
        println("signing.properties found but some entries are missing")
        android.buildTypes.getByName("release").signingConfig = null
    }
} else {
    println("signing.properties not found")
    android.buildTypes.getByName("release").signingConfig = null
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.webkit:webkit:1.9.0")
}
