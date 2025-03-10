plugins {
    id("java")
    id("com.uselessmnemonic.jasm")
    kotlin("jvm") version "1.9.0"
}

group = "com.uselessmnemonic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
