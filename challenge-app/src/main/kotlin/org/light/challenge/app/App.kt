package org.light.challenge.app

import org.light.challenge.logic.core.WorkflowService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.light.challenge.data.*

fun main() {

    // in-memory DB
    val db = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    // TODO: placeholder - start the program here
    val workflowService = WorkflowService()
    transaction(db) {
        SchemaUtils.create(Employees, Rules, Workflow)
    }

    transaction(db) {
    workflowService.addRule(
        WorkflowService.Rule(
            WorkflowService.Department.Marketing,
            Pair(5000.0, 10000.0),
            "marketing_manager",
            WorkflowService.ContactMethod.Email,
    ))}

    transaction(db) {
    val rules = Rules.selectAll().toList()
    for (rule in rules) {
        println("ID: ${rule[Rules.id]}")
        println("Department: ${rule[Rules.department]}")
        println("Min Amount: ${rule[Rules.minAmount]}")
        println("Max Amount: ${rule[Rules.maxAmount]}")
        println("Contact Method: ${rule[Rules.contactMethod]}")
        println("Employee username: ${rule[Rules.employeeUsername]}")
        println()
    }}
}
