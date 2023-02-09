package org.light.challenge.logic.core

enum class Department {
    MARKETING,
    FINANCE
}

enum class ContactMethod {
    EMAIL,
    SLACK
}

data class Invoice(
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
    val department: Department? = null,
    val amountRange: Pair<Double?, Double?> = Pair(null, null),
    val requiresManagerApproval: Boolean? = null,
    val employeeUsername: String,
    val contactMethod: ContactMethod
)

fun toDepartment(input: String): Department? {
    if(input.uppercase() == "FINANCE" || input.uppercase() == "MARKETING")
        return Department.valueOf(input.uppercase())
    return null
}

fun toContactMethod(input: String): ContactMethod {
    return ContactMethod.valueOf(input.uppercase())
}