plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.smali:dexlib2:2.5.2")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.soot-oss:soot:4.6.0")

    constraints {
        implementation("com.google.guava:guava:33.6.0-jre") {
            because("Soot pulls Guava through a dynamic version range; pin it to avoid Maven metadata lookups.")
        }
    }
}

configurations.configureEach {
    resolutionStrategy.force("com.google.guava:guava:33.6.0-jre")
}

application {
    mainClass.set("org.example.Main")
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "org.example.Main"
    }

    archiveClassifier.set("all")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

val capabilitySelfTest by tasks.registering(JavaExec::class) {
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("org.example.CapabilitySelfTest")
}
tasks.check { dependsOn(capabilitySelfTest) }
