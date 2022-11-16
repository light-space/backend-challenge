plugins {
    kotlin("jvm")
}

dependencies {
    // Kotlin libs
    implementation(kotlin("stdlib"))

    implementation(Libraries.money)

    // Logging
    implementation(Libraries.slf_log4j)
    implementation(Libraries.microutils_logging)

    // DB
    implementation(Libraries.jackson_databind)
    implementation(Libraries.exposed)
    implementation(Libraries.sqlite_database)

    // Mockk
    testImplementation(Libraries.mockk)

    // Junit
    testImplementation(Libraries.junit_jupiter_api)
    testRuntimeOnly(Libraries.junit_jupiter_engine)
}
