package org.light.challenge.data

import org.jetbrains.exposed.sql.*

object InvoicesTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val amount = double("amount")
    val department = text("department")
    val requiresManagerApproval = bool("requires_manager_approval")
    val approved = bool("approved")
    val approverUsername = text("approver_username") //made up "approver" term to be concise
}

object EmployeesTable : Table() {
    val username = text("username").uniqueIndex().primaryKey() references InvoicesTable.approverUsername
    val name = text("name")
    val role = text("role")
    val email = text("email")
    val slack = text("slack")
}

object WorkflowTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val department = (text("department")).nullable()
    val minAmount = double("min_amount").nullable()
    val maxAmount = double("max_amount").nullable()
    val requiresManagerApproval = bool("requires_manager_approval").nullable()
    val contactMethod = text("contact_method")
    val employeeUsername = (text("employee_username") references EmployeesTable.username)
}