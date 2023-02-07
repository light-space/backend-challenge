package org.light.challenge.logic.core

class TypeHelper {
    enum class Department {
        MARKETING,
        FINANCE
    }

    enum class ContactMethod {
        EMAIL,
        SLACK
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
        val requiresManagerApproval: Boolean,
        val employeeUsername: String,
        val contactMethod: ContactMethod
    )

    fun toDepartment(input: String): Department {
        return Department.valueOf(input.uppercase())
    }

    fun toContactMethod(input: String): ContactMethod {
        return ContactMethod.valueOf(input.uppercase())
    }

}