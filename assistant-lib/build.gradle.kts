plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "dev.mtib"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.aallam.openai:openai-client-bom:3.7.1"))
    implementation("com.aallam.openai:openai-client")
    runtimeOnly("io.ktor:ktor-client-okhttp")

    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    implementation("com.charleskorn.kaml:kaml:0.58.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-Beta")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
