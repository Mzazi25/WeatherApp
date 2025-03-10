plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.mapsplatform.secrets.gradle.plugin)
    kotlin("kapt")
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    jacoco
}

jacoco {
    toolVersion = "0.8.8"
}

project.afterEvaluate {
    setupAndroidReporting()
}

fun setupAndroidReporting() {
    val buildTypes = listOf("debug")

    buildTypes.forEach { buildTypeName ->
        val sourceName = buildTypeName
        val testTaskName = "test${sourceName.capitalize()}UnitTest"
        println("Task -> $testTaskName")

        tasks.register<JacocoReport>("${testTaskName}Coverage") {
            dependsOn(tasks.findByName(testTaskName))

            group = "Reporting"
            description = "Generate Jacoco coverage reports on the ${sourceName.capitalize()} build."

            reports {
                xml.required.set(true)
                csv.required.set(false)
                html.required.set(true)
            }

            val fileFilter = listOf(
                // android
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
                "android/**/*.*",
                // kotlin
                "**/*MapperImpl*.*",
                "**/*\$ViewInjector*.*",
                "**/*\$ViewBinder*.*",
                "**/BuildConfig.*",
                "**/*Component*.*",
                "**/*BR*.*",
                "**/Manifest*.*",
                "**/*\$Lambda$*.*",
                "**/*Companion*.*",
                "**/*Module*.*",
                "**/*Dagger*.*",
                "**/*Hilt*.*",
                "**/*MembersInjector*.*",
                "**/*_MembersInjector.class",
                "**/*_Factory*.*",
                "**/*_Provide*Factory*.*",
                "**/*Extensions*.*",
                // sealed and data classes
                "**/*\$Result.*",
                "**/*\$Result$*.*",
                // adapters generated by moshi
                "**/*JsonAdapter.*",
                "**/*Activity*",
                "**/di/**",
                "**/hilt*/**",
                "**/entrypoint/**",
                "**/theme/**",
                "**/*Screen*.*"
            )

            val javaTree = fileTree("${project.buildDir}/intermediates/javac/$sourceName/classes"){
                exclude(fileFilter)
            }
            val kotlinTree = fileTree("${project.buildDir}/tmp/kotlin-classes/$sourceName"){
                exclude(fileFilter)
            }
            classDirectories.setFrom(files(javaTree, kotlinTree))

            executionData.setFrom(files("${project.buildDir}/jacoco/${testTaskName}.exec"))
            val coverageSourceDirs = listOf(
                "${project.projectDir}/src/main/java",
                "${project.projectDir}/src/$buildTypeName/java"
            )

            sourceDirectories.setFrom(files(coverageSourceDirs))
            additionalSourceDirs.setFrom(files(coverageSourceDirs))
        }
    }
}

android {
    namespace = "com.github.odaridavid.weatherapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.github.odaridavid.weatherapp"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn" + listOf(
            "-P",
            // See https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/compiler-metrics.md
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir}/reports/kotlin-compile/compose"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Jetpack Core
    implementation(libs.bundles.androidx)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    // Google Play Services
    implementation(libs.playservices.location)

    // Data & Async
    implementation(libs.retrofit)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization)
    implementation(libs.coil)

    // DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.mock.android)
    testImplementation(libs.mock.agent)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    
    // Chucker
    debugImplementation(libs.chucker.debug)
    releaseImplementation(libs.chucker.release)
}

kapt {
    correctErrorTypes = true
}
