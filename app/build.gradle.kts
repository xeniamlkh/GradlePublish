plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gradlepublish"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.example.gradlepublish"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

abstract class PublishingExtension(objects: ObjectFactory) {

    val track: Property<String> =
        objects.property(String::class.java)
            .convention("internal")

    val rolloutPercentage: Property<Int> =
        objects.property(Int::class.javaObjectType)
            .convention(100)

    val releaseNotes: Property<String> =
        objects.property(String::class.java)
            .convention("Initial release")
}

val appPublishing = project.extensions.create(
    "appPublishing",
    PublishingExtension::class.java,
    project.objects
)

appPublishing.apply {
    track.set("internal")
    rolloutPercentage.set(20)
    releaseNotes.set("First release")
}

abstract class PublishAppTask : DefaultTask() {

    @get:Input
    abstract val track: Property<String>

    @get:Input
    abstract val rolloutPercentage: Property<Int>

    @get:Input
    abstract val releaseNotes: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val artifactFile: RegularFileProperty

    @TaskAction
    fun publish() {
        val artifact = artifactFile.get().asFile

        require(artifact.exists()) {
            "APK/AAB file does not exist!"
        }

        val allowedTracks = setOf(
            "internal",
            "alpha",
            "beta",
            "production"
        )

        require(track.get() in allowedTracks) {
            "Invalid track: ${track.get()}"
        }

        require(rolloutPercentage.get() in 1..100) {
            "Rollout percentage must be between 1 and 100"
        }

        logger.lifecycle("Preparing application publication...")

        logger.lifecycle("Track: ${track.get()}")

        logger.lifecycle(
            "Rollout: ${rolloutPercentage.get()}%"
        )

        logger.lifecycle(
            "Release notes: ${releaseNotes.get()}"
        )

        logger.lifecycle(
            "Artifact: ${artifact.absolutePath}"
        )

        logger.lifecycle("Publication completed!")
    }
}

tasks.register<PublishAppTask>("publishApp") {

    group = "publishing"

    description = "Publishes application to Google Play"

    dependsOn("assembleDebug")

    track.set(appPublishing.track)

    rolloutPercentage.set(
        appPublishing.rolloutPercentage
    )

    releaseNotes.set(
        appPublishing.releaseNotes
    )

    artifactFile.set(
        project.layout.buildDirectory.file(
            "outputs/apk/debug/app-debug.apk"
        )
    )
}