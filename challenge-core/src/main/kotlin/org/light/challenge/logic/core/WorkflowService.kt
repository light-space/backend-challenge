package org.light.challenge.logic.core

import org.jetbrains.exposed.sql.*
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
        val matchedRule = RulesTable.select {
            (RulesTable.department eq invoice.department.name or RulesTable.department.isNull()) and
            (RulesTable.minAmount less  invoice.amount or RulesTable.minAmount.isNull()) and
            (RulesTable.maxAmount greaterEq  invoice.amount or RulesTable.maxAmount.isNull()) and
            (RulesTable.requiresManagerApproval eq invoice.requiresManagerApproval or RulesTable.requiresManagerApproval.isNull())
        }.firstOrNull()

        if(matchedRule != null){
            return "Send a message via ${matchedRule[RulesTable.contactMethod]} to ${matchedRule[RulesTable.employeeUsername]}"
        }
        return "Warning: This invoice did not match with any rule of the workflow. Sending approval request to default employee."
    }
}