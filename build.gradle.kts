plugins {
    application
    kotlin("jvm") version "2.0.21"
}
application {
    mainClass.set("org.example.MainKt")
}
group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.18.1")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}