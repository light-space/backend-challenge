package org.light.challenge.app

import org.light.challenge.logic.core.WorkflowService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.light.challenge.data.*
import org.light.challenge.logic.core.TypeHelper
import org.light.challenge.logic.core.TypeHelper.*

fun main() {

    // in-memory DB
    val db = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    // TODO: placeholder - start the program here
    val workflowService = WorkflowService()
    val typeHelper = TypeHelper()

    println("Enter a rule")
    println("Department: ")
    val department = readLine()
    println("Minimum amount: ")
    val minAmount = readln().toDouble()
    println("Maximum amount: ")
    val maxAmount = readln().toDouble()
    println("Contact method: ")
    val contactMethod = readln()
    println("Employee username: ")
    val employeeUsername = readln()

    // Manually inputting a rule and rule id inside workflow
    transaction(db) {

        SchemaUtils.create(EmployeesTable, RulesTable, WorkflowTable)

        val ruleId = workflowService.addRule(
            Rule(
                typeHelper.toDepartment(department!!),
                Pair(minAmount, maxAmount),
                employeeUsername,
                typeHelper.toContactMethod(contactMethod),
            )
        )

        workflowService.addRuleIdToWorkflow(ruleId)

        val rules = RulesTable.selectAll().toList()
        for (rule in rules) {
            println("ID: ${rule[RulesTable.id]}")
            println("Department: ${rule[RulesTable.department]}")
            println("Min Amount: ${rule[RulesTable.minAmount]}")
            println("Max Amount: ${rule[RulesTable.maxAmount]}")
            println("Contact Method: ${rule[RulesTable.contactMethod]}")
            println("Employee username: ${rule[RulesTable.employeeUsername]}")
            println()
        }

        val workFlow = WorkflowTable.selectAll().toList()
        println()
        for (ruleId in workFlow) {
            println("RuleId: ${ruleId[WorkflowTable.ruleIds]}")
        }
    }
}
