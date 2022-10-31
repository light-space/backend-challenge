package org.light.challenge.app

import org.jetbrains.exposed.sql.Database

fun main() {

    // in-memory DB
    val db = Database.connect("jdbc:sqlite::memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    // TODO: placeholder - start the program here
}
