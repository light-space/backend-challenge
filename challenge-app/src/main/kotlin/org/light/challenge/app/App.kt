package org.light.challenge.app

import org.light.challenge.logic.core.WorkflowService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.light.challenge.data.*
import org.light.challenge.logic.core.TypeHelper
import org.light.challenge.logic.core.TypeHelper.*

fun main(args: Array<String>) {

    // in-memory DB
    val db = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    transaction(db) {
        SchemaUtils.create(EmployeesTable, RulesTable, WorkflowTable)
    }

    val workflowService = WorkflowService()
    val typeHelper = TypeHelper()
    // TODO: placeholder - start the program here

    when (args[0]) {
          "--submit-invoice" -> {
            if(args.size == 4){
                val newInvoice = Invoice(
                    amount = args[1].toDouble(),
                    department = typeHelper.toDepartment(args[2]),
                    requiresManagerApproval = args[3].toBoolean()
                )
                transaction(db) {
                    val sendInvoiceTo = workflowService.processInvoice(newInvoice)
                    println(sendInvoiceTo)
                }
            } else {
                println("""Usage: ./gradlew run --args="--submit-invoice <amount> <department> <managerApproval>"""")
                return
            }
        }
        "--delete-workflow" -> {
            transaction(db) {
                workflowService.deleteWorkflow()
            }
        }
        "--add-rule-to-workflow" -> {
            if(args.size == 6) {
                transaction(db) {

                    val ruleId = workflowService.addRule(
                        Rule(
                            amountRange = Pair(args[0].toDouble(), args[1].toDouble()),
                            department = typeHelper.toDepartment(args[2]),
                            requiresManagerApproval = args[3].toBoolean(),
                            employeeUsername = args[4],
                            contactMethod = typeHelper.toContactMethod(args[5])
                    ))
                    workflowService.addRuleIdToWorkflow(ruleId)
                }
            } else {
                println("""Usage: ./gradlew run --args="--add-rule-to-workflow <min_amount> <max_amount> <department [Marketing/Finance]>
                    |      <require_manager_approval> <receiver_username> <contact_method [email/slack]>"""")
                return
            }
        }
    }

    /*
    println("Enter a rule")
    println("Department: ")
    val department = readLine()
    println("Minimum amount: ")
    val minAmount = readln().toDouble()
    println("Maximum amount: ")
    val maxAmount = readln().toDouble()
    println("Requires Manager Approval [true / false]: ")
    val requiresManagerApproval = readln().toBoolean()
    println("Contact method: ")
    val contactMethod = readln()
    println("Employee username: ")
    val employeeUsername = readln()

*/      transaction(db) {
        val rules = RulesTable.selectAll().toList()
        for (rule in rules) {
            println("ID: ${rule[RulesTable.id]}")
            println("Department: ${rule[RulesTable.department]}")
            println("Min Amount: ${rule[RulesTable.minAmount]}")
            println("Max Amount: ${rule[RulesTable.maxAmount]}")
            println("Manager Approval: ${rule[RulesTable.requiresManagerApproval]}")
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
