package org.light.challenge.logic.core

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

}
