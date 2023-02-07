package org.light.challenge.logic.core

import org.light.challenge.data.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
class WorkflowService {
    // TODO: placeholder - workflow logic here
    enum class Department {
        Marketing,
        Finance
    }

    enum class ContactMethod {
        Email,
        Slack
    }

    data class Invoice(
        val id: Int,
        val amount: Double,
        val department: Department,
        val requiresManagerApproval: Boolean
    )

    data class Employee(
        val username: String,
        val name: String,
        val role: String,
        val email: String,
        val slack: String
    )

    data class Rule(
        val department: Department,
        val amountRange: Pair<Double, Double>,
        val employeeUsername: String,
        val contactMethod: ContactMethod
    )

     fun addRule(rule: Rule): Int {
        val newRuleId = Rules.insert {
            it[department] = rule.department.name
            it[minAmount] = rule.amountRange.first
            it[maxAmount] = rule.amountRange.second
            it[contactMethod] = rule.contactMethod.name
            it[employeeUsername] = rule.employeeUsername
        } get Rules.id

        return newRuleId
     }

    fun addRuleIdToWorkflow(ruleId: Int) {
        Workflow.insert {
            it[ruleIds] = ruleId
        }
    }

}
