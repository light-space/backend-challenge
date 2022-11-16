plugins {
    kotlin("jvm")
    application
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    mainClass.set("org.light.challenge.app.AppKt")
}

dependencies {
    implementation(project(":challenge-core"))
    implementation(project(":challenge-data"))

    // Kotlin libs
    implementation(kotlin("stdlib"))

    // DB
    implementation(Libraries.jackson_databind)
    implementation(Libraries.jackson_kotlin)
    implementation(Libraries.jackson_zalando)
    implementation(Libraries.exposed)
    implementation(Libraries.sqlite_database)

    // Logging
    implementation(Libraries.slf_log4j)
    implementation(Libraries.microutils_logging)

    implementation(Libraries.money)

    // Mockk
    testImplementation(Libraries.mockk)

    // Junit
    testImplementation(Libraries.junit_jupiter_api)
    testRuntimeOnly(Libraries.junit_jupiter_engine)
}
