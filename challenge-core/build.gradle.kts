plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":challenge-data"))

    // Kotlin libs
    implementation(kotlin("stdlib"))

    // Logging
    implementation(Libraries.slf_log4j)
    implementation(Libraries.microutils_logging)

    implementation(Libraries.money)
    implementation(Libraries.coroutines)

    // Mockk
    testImplementation(Libraries.mockk)

    // Junit
    testImplementation(Libraries.junit_jupiter_api)
    testRuntimeOnly(Libraries.junit_jupiter_engine)
}
