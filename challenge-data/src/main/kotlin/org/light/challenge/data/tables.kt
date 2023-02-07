package org.light.challenge.data

// TODO: placeholder - DB tables here
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Invoices : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val amount = double("amount")
    val department = text("department")
    val requiresManagerApproval = bool("requires_manager_approval")
}

object Employees : Table() {
    val username = text("username").uniqueIndex().primaryKey()
    val name = text("name")
    val role = text("role")
    val email = text("email")
    val slack = text("slack")
}

object Rules : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val department = (text("department"))
    val minAmount = double("min_amount")
    val maxAmount = double("max_amount")
    val employeeUsername = (text("employee_username") references Employees.username)
}

fun createTables() {
    Database.connect("jdbc:sqlite::memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Invoices, Employees, Rules)
    }
}
