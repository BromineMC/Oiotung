plugins {
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "ru.brominemc.oiotung"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.tinylog:tinylog-api-kotlin:2.6.2")
    implementation("org.tinylog:tinylog-impl:2.6.2")
}

kotlin {
    jvmToolchain(21)
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
