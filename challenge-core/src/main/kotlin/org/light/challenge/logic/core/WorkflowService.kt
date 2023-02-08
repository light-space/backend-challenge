package org.light.challenge.logic.core

import org.light.challenge.data.*
import org.light.challenge.logic.core.TypeHelper.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class WorkflowService {
    // TODO: placeholder - workflow logic here

     fun addRule(rule: Rule): Int {
        val newRuleId = RulesTable.insert {
            it[department] = rule.department.name
            it[minAmount] = rule.amountRange.first
            it[maxAmount] = rule.amountRange.second
            it[requiresManagerApproval] = rule.requiresManagerApproval
            it[contactMethod] = rule.contactMethod.name
            it[employeeUsername] = rule.employeeUsername
        } get RulesTable.id

        return newRuleId
     }

    fun addRuleIdToWorkflow(ruleId: Int) {
        WorkflowTable.insert {
            it[ruleIds] = ruleId
        }
    }

    fun addInvoice(invoice: Invoice) {
        InvoicesTable.insert {
            it[id] = invoice.id
            it[amount] = invoice.amount
            it[department] = invoice.department.name
            it[requiresManagerApproval] = invoice.requiresManagerApproval
        }
    }

    fun processInvoice(invoice: Invoice): String {
        val workflow = WorkflowTable.selectAll().toList()
        val ruleIds = workflow.map { it[WorkflowTable.ruleIds] }

        for (ruleId in ruleIds) {
            val rule = RulesTable.select { RulesTable.id eq ruleId }.first()
            val department = Department.valueOf(rule[RulesTable.department])
            val minAmount = rule[RulesTable.minAmount]
            val maxAmount = rule[RulesTable.maxAmount]
            val requiresManagementApproval = rule[RulesTable.requiresManagerApproval]
            val contactMethod = ContactMethod.valueOf(rule[RulesTable.contactMethod])
            val employeeUsername = rule[RulesTable.employeeUsername]

            if (invoice.department === department &&
                invoice.amount in minAmount..maxAmount &&
                invoice.requiresManagerApproval == requiresManagementApproval
            ) {
                return "Send a message via $contactMethod to $employeeUsername"
            }
        }
        return "None of the rules passed."
    }
}