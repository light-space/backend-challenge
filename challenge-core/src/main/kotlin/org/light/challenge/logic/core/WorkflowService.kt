package org.light.challenge.logic.core

import org.jetbrains.exposed.sql.*
import org.light.challenge.data.*

class WorkflowService {

     fun addRule(rule: Rule) {
         WorkflowTable.insert {
            it[department] = rule.department?.name
            it[minAmount] = rule.amountRange.first
            it[maxAmount] = rule.amountRange.second
            it[requiresManagerApproval] = rule.requiresManagerApproval
            it[contactMethod] = rule.contactMethod.name
            it[employeeUsername] = rule.employeeUsername
        }
     }

    private fun addEmployee(employee: Employee) {
        EmployeesTable.insert {
            it[username] = employee.username
            it[name] = employee.name
            it[role] = employee.role
            it[email] = employee.email
            it[slack] = employee.slack
        }
    }

    private fun addInvoice(invoice: Invoice, approver: String): Int {
        val invoiceId = InvoicesTable.insert {
            it[amount] = invoice.amount
            it[department] = invoice.department.name
            it[requiresManagerApproval] = invoice.requiresManagerApproval
            it[approved] = false
            it[approverUsername] = approver
        } get InvoicesTable.id
        return invoiceId
    }

    fun generateEmployeeTable() {
        addEmployee( Employee("lsimon", "Lluis Simon", "Finance Member", "lluis.simon.92@gmail.com", "lluis.simon.92" ) )
        addEmployee( Employee("jsanders", "Jonathan Sanders", "CMO", "jonathan@light.inc", "jonathan" ) )
        addEmployee( Employee("fkozjak", "Filip Kozjak", "CFO", "filip@light.inc", "filip" ) )
        addEmployee( Employee("jcop", "Jelena Cop", "Finance Manager", "jelena@light.inc", "jelena" ) )
        addEmployee( Employee("meetsoon", "Meeting Soon", "Finance Member", "hope.to.meet.soon@light.inc", "hope.to.meet.soon" ) )
    }

    fun deleteEmployeeTable() {
        EmployeesTable.deleteAll()
    }

    fun deleteWorkflow(){
        WorkflowTable.deleteAll()
    }

    fun printWorkflow(){
        val rules = WorkflowTable.selectAll().toList()
        println("${WorkflowTable.id.name} | ${WorkflowTable.department.name} | ${WorkflowTable.minAmount.name} | " +
                "${WorkflowTable.maxAmount.name} | ${WorkflowTable.requiresManagerApproval.name} | " +
                "${WorkflowTable.contactMethod.name} | ${WorkflowTable.employeeUsername.name}")
        for (rule in rules) {
            println()
            println("${rule[WorkflowTable.id]} | ${rule[WorkflowTable.department]} | ${rule[WorkflowTable.minAmount]} | " +
                    "${rule[WorkflowTable.maxAmount]} | ${rule[WorkflowTable.requiresManagerApproval]} | " +
                    "${rule[WorkflowTable.contactMethod]} | ${rule[WorkflowTable.employeeUsername]}")
            println()
        }
    }

    private fun findMatchingRuleQuery(invoice: Invoice): ResultRow? {
        val totalTable = (EmployeesTable innerJoin WorkflowTable)
        return(
            totalTable.select {
                (WorkflowTable.department eq invoice.department.name or WorkflowTable.department.isNull()) and
                (WorkflowTable.minAmount less  invoice.amount or WorkflowTable.minAmount.isNull()) and
                (WorkflowTable.maxAmount greaterEq  invoice.amount or WorkflowTable.maxAmount.isNull()) and
                (WorkflowTable.requiresManagerApproval eq invoice.requiresManagerApproval or WorkflowTable.requiresManagerApproval.isNull())
            }.firstOrNull()
    )}

    fun processInvoice(invoice: Invoice): String {
        val matchedRule = findMatchingRuleQuery(invoice)
        val invoiceId = addInvoice(invoice, matchedRule?.get(EmployeesTable.username) ?: "jsanders")
        if(matchedRule != null) {
            return "${matchedRule[WorkflowTable.contactMethod]}, Sending approval request for " +
                   "invoice #$invoiceId to ${matchedRule[EmployeesTable.name]}.\n" +
                   "${if(matchedRule[WorkflowTable.contactMethod] == "SLACK") "Slack user: ${matchedRule[EmployeesTable.slack]}"
                      else "Email: ${matchedRule[EmployeesTable.email]}"}\n" +
                   "Role: ${matchedRule[EmployeesTable.role]}"
        }
        return "Warning: This invoice did not match any rule of the workflow. Sending approval request to Jonathan Sanders."
    }
}