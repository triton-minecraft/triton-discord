plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.kyriji"
version = ""

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.2.2")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.mongodb:mongodb-driver-sync:5.3.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "dev.kyriji.discord.TritonDiscord"
        }
    }

    shadowJar {
        archiveClassifier.set("")
    }
}
