package org.light.challenge.app

import org.light.challenge.logic.core.WorkflowService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.light.challenge.data.*
import org.light.challenge.logic.core.*
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Error: Please provide arguments. Valid options are")
        println("  --submit-invoice <amount[Double]> <department[MARKETING/FINANCE]> <manager_approval[Boolean]>")
        println("  --add-rule-to-workflow")
        println("  --delete-workflow")
        println("  --print-workflow")
        println("""  Usage: ./gradlew run --args="--option arg1 arg2 .... """")
        return
    }
    // in-memory DB
    val db = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    transaction(db) {
        SchemaUtils.create(EmployeesTable, WorkflowTable, InvoicesTable)
        WorkflowService().generateEmployeeTable()
    }
    when (args[0]) {
          "--submit-invoice" -> {
            try {
                transaction(db) {
                    val sendInvoiceTo = WorkflowService().processInvoice(
                        Invoice(
                            amount = args[1].toDouble(),
                            department = toDepartment(args[2])!!,
                            requiresManagerApproval = args[3].toBooleanStrictOrNull()!!
                        )
                    )
                    println(sendInvoiceTo)
                }
            } catch (e: Exception){
                println("Error: Invoice format or argument type is not correct.")
                println("""   Usage: ./gradlew run --args="--submit-invoice <amount> <department> <manager_approval>"""")
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
                val department = toDepartment(readln())
                println("Requires Manager Approval [true / false]: ")
                val requiresManagerApproval = readln().toBooleanStrictOrNull()
                println("Employee username: ")
                val employeeUsername = readln()
                println("Contact method: ")
                val contactMethod = toContactMethod(readln())
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
        "--print-workflow" -> {
            try{
                transaction(db) {
                    WorkflowService().printWorkflow()
                }
            } catch(e:Exception){
                println("Error: The workflow could not be printed.")
                println("""   Usage: ./gradlew run --args="--print-workflow"""")
            }
        }
        else -> {
            println("""Error: Unknown command line option "${args[0]}". Valid options are:""")
            println("  --submit-invoice <amount[Double]> <department[MARKETING/FINANCE]> <manager_approval[Boolean]>")
            println("  --add-rule-to-workflow")
            println("  --delete-workflow")
            println("  --print-workflow")
        }
    }
    transaction(db) {
        WorkflowService().deleteEmployeeTable()
    }
}
