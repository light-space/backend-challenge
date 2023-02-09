package org.light.challenge.logic.core

import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.light.challenge.data.*

class WorkflowService {

    private fun addRuleIdToWorkflow(ruleId: Int) {
        WorkflowTable.insert {
            it[ruleIds] = ruleId
        }
    }
     fun addRule(rule: Rule) {
        val newRuleId = RulesTable.insert {
            it[department] = rule.department?.name
            it[minAmount] = rule.amountRange.first
            it[maxAmount] = rule.amountRange.second
            it[requiresManagerApproval] = rule.requiresManagerApproval
            it[contactMethod] = rule.contactMethod.name
            it[employeeUsername] = rule.employeeUsername
        } get RulesTable.id
         addRuleIdToWorkflow(newRuleId)
     }

    fun addInvoice(invoice: Invoice) {
        InvoicesTable.insert {
            it[id] = invoice.id
            it[amount] = invoice.amount
            it[department] = invoice.department.name
            it[requiresManagerApproval] = invoice.requiresManagerApproval
        }
    }

    fun deleteWorkflow(){
        RulesTable.deleteAll()
        WorkflowTable.deleteAll()
    }

    fun printWorkflow(){
        val rules = RulesTable.selectAll().toList()
        println("${RulesTable.id.name} | ${RulesTable.department.name} | ${RulesTable.minAmount.name} | " +
                "${RulesTable.maxAmount.name} | ${RulesTable.requiresManagerApproval.name} | " +
                "${RulesTable.contactMethod.name} | ${RulesTable.employeeUsername.name}")
        for (rule in rules) {
            println()
            println("${rule[RulesTable.id]} | ${rule[RulesTable.department]} | ${rule[RulesTable.minAmount]} | " +
                    "${rule[RulesTable.maxAmount]} | ${rule[RulesTable.requiresManagerApproval]} | " +
                    "${rule[RulesTable.contactMethod]} | ${rule[RulesTable.employeeUsername]}")
            println()
        }
    }

    fun processInvoice(invoice: Invoice): String {
        val workflow = WorkflowTable.selectAll().toList()
        val ruleIds = workflow.map { it[WorkflowTable.ruleIds] }

        for (ruleId in ruleIds) {
            val rule = RulesTable.select { RulesTable.id eq ruleId }.first()
            val department = rule[RulesTable.department]?.let { Department.valueOf(it) }
            val minAmount = rule[RulesTable.minAmount]
            val maxAmount = rule[RulesTable.maxAmount]
            val requiresManagementApproval = rule[RulesTable.requiresManagerApproval]
            val contactMethod = ContactMethod.valueOf(rule[RulesTable.contactMethod])
            val employeeUsername = rule[RulesTable.employeeUsername]

            if ( (department == null || invoice.department === department) &&
                 (minAmount == null || invoice.amount > minAmount) &&
                 (maxAmount == null || invoice.amount <= maxAmount) &&
                 (requiresManagementApproval == null || invoice.requiresManagerApproval == requiresManagementApproval)
            ){
                return "Send a message via $contactMethod to $employeeUsername"
            }
        }
        return "Warning: This invoice did not match with any rule of the workflow. Sending approval request to default employee."
    }
}