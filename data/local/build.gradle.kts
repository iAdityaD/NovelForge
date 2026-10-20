plugins { id("com.android.library"); id("org.jetbrains.kotlin.android"); id("com.google.devtools.ksp") }
android { namespace="com.novelforge.data.local"; compileSdk=35; defaultConfig { minSdk=26 }; compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }; kotlinOptions { jvmTarget="17" } }
dependencies {
 implementation(project(":core:model")); implementation(project(":core:database"))
 implementation(project(":domain:workspace"))
 implementation(project(":domain:writing"))
 implementation(project(":domain:story"))
 implementation(project(":domain:analytics"))
 implementation(project(":domain:backup"))
 implementation(project(":domain:preferences"))

 implementation("androidx.room:room-runtime:2.7.2"); implementation("androidx.room:room-ktx:2.7.2")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
 implementation("com.google.dagger:hilt-android:2.55"); ksp("com.google.dagger:hilt-compiler:2.55")
}
