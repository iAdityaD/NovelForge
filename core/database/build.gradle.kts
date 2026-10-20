plugins { id("com.android.library"); id("org.jetbrains.kotlin.android"); id("com.google.devtools.ksp") }
android {
 namespace = "com.novelforge.core.database"
 compileSdk = 35
 defaultConfig { minSdk = 26 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 implementation(project(":core:model"))
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
 implementation("com.google.dagger:hilt-android:2.55")
 ksp("com.google.dagger:hilt-compiler:2.55")
}

dependencies { implementation("androidx.room:room-runtime:2.7.2"); implementation("androidx.room:room-ktx:2.7.2"); ksp("androidx.room:room-compiler:2.7.2") }
ksp { arg("room.schemaLocation", "${rootProject.projectDir}/app/schemas") }

dependencies { implementation(project(":domain:workspace")) }
