plugins {
    kotlin("jvm")
    application
}

group = "dev.mtib"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":assistant-lib"))
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-Beta")
    implementation("info.picocli:picocli:4.7.5")
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("dev.mtib.dnd.ai.commands.DndAssistantKt")
}

task("fatJar", type = Jar::class) {
    archiveBaseName = "${project.name}-fat"
    manifest {
        attributes["Implementation-Title"] = "Fat jar of ${project.name}"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "dev.mtib.dnd.ai.commands.DndAssistantKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}
