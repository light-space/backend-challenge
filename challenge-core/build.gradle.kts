plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":challenge-data"))

    // Kotlin libs
    implementation(kotlin("stdlib"))

    // DB
    implementation(Libraries.exposed)
    implementation(Libraries.sqlite_database)

    // Logging
    implementation(Libraries.slf_log4j)
    implementation(Libraries.microutils_logging)

    // Mockk
    testImplementation(Libraries.mockk)

    // Junit
    testImplementation(Libraries.junit_jupiter_api)
    testRuntimeOnly(Libraries.junit_jupiter_engine)
}
