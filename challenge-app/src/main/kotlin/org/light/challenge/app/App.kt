package org.light.challenge.app

import org.light.challenge.logic.core.WorkflowService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.light.challenge.data.*
import org.light.challenge.logic.core.TypeHelper
import org.light.challenge.logic.core.TypeHelper.*
import java.io.File

fun main(args: Array<String>) {

    // in-memory DB
    val db = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    transaction(db) {
        SchemaUtils.create(EmployeesTable, RulesTable, WorkflowTable)
    }
    // TODO: placeholder - start the program here
    when (args[0]) {
          "--submit-invoice" -> {
            try {
                transaction(db) {
                    val sendInvoiceTo = WorkflowService().processInvoice(
                        Invoice(
                            amount = args[1].toDouble(),
                            department = TypeHelper().toDepartment(args[2])!!,
                            requiresManagerApproval = args[3].toBooleanStrictOrNull()!!
                        )
                    )
                    println(sendInvoiceTo)
                }
                return
            } catch (e: Exception){
                println("Error: Invoice format or argument type is not correct.")
                println("""   Usage: ./gradlew run --args="--submit-invoice <amount> <department> <managerApproval>"""")
            }
        }
        "--delete-workflow" -> {
            try {
                transaction(db) {
                    WorkflowService().deleteWorkflow()
                }
                transaction(db){close()}
                File("./memory").delete()
                println("Workflow successfully deleted.")
                return
            } catch(e: Exception){
                println("Error: Something went wrong. Workflow was not deleted.")
                println("""   Usage: ./gradlew run --args="--delete-workflow"""")
            }
        }
        "--add-rule-to-workflow" -> {
            while(true) {
                println("Enter the next rule in the workflow. Press enter if you wish to skip a constraint.")
                println("Minimum amount: ")
                val minAmount = readln().toDoubleOrNull()
                println("Maximum amount: ")
                val maxAmount = readln().toDoubleOrNull()
                println("Department: ")
                val department = TypeHelper().toDepartment(readln())
                println("Requires Manager Approval [true / false]: ")
                val requiresManagerApproval = readln().toBooleanStrictOrNull()
                println("Employee username: ")
                val employeeUsername = readln()
                println("Contact method: ")
                val contactMethod = TypeHelper().toContactMethod(readln())
                try {
                    transaction(db) {
                         WorkflowService().addRule(
                            Rule(
                                amountRange = Pair(minAmount, maxAmount),
                                department = department,
                                requiresManagerApproval = requiresManagerApproval,
                                employeeUsername = employeeUsername,
                                contactMethod = contactMethod
                            )
                        )
                    }
                    println("Rule successfully added into workflow.")
                } catch(e:Exception) {
                    println("Error: Last rule could not be added. Make sure you entered the correct value types:")
                    println("  Minimum amount: Number")
                    println("  Maximum amount: Number")
                    println("  Department: [Finance/Marketing]")
                    println("  Require Manager Approval: [true/false]")
                    println("  Employee Name: Text")
                    println("  Contact Method: [email/slack]")
                }
                println("Do you wish to add another rule? [y/n]")
                if(readln() == "y") continue else break
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
