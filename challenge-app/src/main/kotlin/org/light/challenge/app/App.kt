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
        println(
            """
            Error: Please provide arguments. Valid options are:
              --submit-invoice <amount[Double]> <department[MARKETING/FINANCE]> <manager_approval[Boolean]>
              --add-rule-to-workflow
              --delete-workflow
              --print-workflow
              Usage: ./gradlew run --args="--option arg1 arg2 .... "
            """.trimIndent()
        )
        return
    }
    // in-memory DB
    val db = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    transaction(db) {
        SchemaUtils.create(EmployeesTable, WorkflowTable, InvoicesTable)
    }
    when (args[0]) {
          "--submit-invoice" -> {
            try {
                transaction(db) {
                    WorkflowService().generateEmployeeTable()
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
                    WorkflowService().deleteEmployeeTable()
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
                println(" $minAmount")
                println("Maximum amount: ")
                val maxAmount = readln().toDoubleOrNull()
                println(" $maxAmount")
                println("Department [finance/marketing]: ")
                val department = toDepartment(readln())
                println(" $department")
                println("Requires Manager Approval [true/false]: ")
                val requiresManagerApproval = readln().toBooleanStrictOrNull()
                println(" $requiresManagerApproval")
                println("Username of the employee to approve: ")
                val employeeUsername = readln()
                println(" $employeeUsername")
                println("Contact method [email/slack]: ")
                val contactMethod = toContactMethod(readln())
                println(" $contactMethod")
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
                    println(
                        """
                        Error: Last rule could not be added. Make sure you entered the correct value types:
                          Minimum amount: Number
                          Maximum amount: Number
                          Department: [Finance/Marketing]
                          Require Manager Approval: [true/false]
                          Employee Name: Text
                          Contact Method: [email/slack]
                        """.trimIndent()
                    )
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
            println(
                """
                Error: Unknown command line option "${args[0]}". Valid options are:
                  --submit-invoice <amount[Double]> <department[MARKETING/FINANCE]> <manager_approval[Boolean]>
                  --add-rule-to-workflow
                  --delete-workflow
                  --print-workflow
                """".trimIndent()
            )
        }
    }
    transaction(db) {
        WorkflowService().deleteEmployeeTable()
    }
}
