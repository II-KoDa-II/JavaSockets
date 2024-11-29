plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.slf4j:slf4j-api:1.7.36")
    runtimeOnly("ch.qos.logback:logback-core:1.4.14")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.12")
}

tasks.test {
    useJUnitPlatform()
}