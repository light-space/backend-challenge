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
                transaction(db) {
                    val sendInvoiceTo = workflowService.processInvoice(
                        Invoice(
                            amount = args[1].toDouble(),
                            department = typeHelper.toDepartment(args[2]),
                            requiresManagerApproval = args[3].toBoolean())
                    )
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
            while(true) {
                println("Enter the next rule in the workflow. Press enter if you wish to skip a constraint.")
                println("Minimum amount: ")
                val minAmount = readln().toDouble()
                println("Maximum amount: ")
                val maxAmount = readln().toDouble()
                println("Department: ")
                val department = typeHelper.toDepartment(readln())
                println("Requires Manager Approval [true / false]: ")
                val requiresManagerApproval = readln().toBoolean()
                println("Employee username: ")
                val employeeUsername = readln()
                println("Contact method: ")
                val contactMethod = typeHelper.toContactMethod(readln())

                transaction(db) {
                    val ruleId = workflowService.addRule(
                        Rule(
                            amountRange = Pair(minAmount, maxAmount),
                            department = department,
                            requiresManagerApproval = requiresManagerApproval,
                            employeeUsername = employeeUsername,
                            contactMethod = contactMethod
                        )
                    )
                    workflowService.addRuleIdToWorkflow(ruleId)
                }
                println("Rule successfully included in the workflow, do you wish to enter another rule? [y/n]")
                val wishToContinue = readln()
                if(wishToContinue == "y") continue else break;
            }
        }
    }

      transaction(db) {
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
