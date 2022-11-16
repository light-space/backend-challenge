package org.light.challenge.data.models

import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.javamoney.moneta.Money

data class WorkflowStepDao(
    val id: WorkflowStepId,
    val workflowRule: WorkflowRule
) {
    fun type() = workflowRule.type
    fun params() = workflowRule.params

    fun onSuccess() = workflowRule.onSuccess
    fun onFailure() = workflowRule.onFailure
    fun startStep() = workflowRule.startStep
}

data class WorkflowRule(
    val companyId: CompanyId,
    val type: WorkflowRuleType,
    val params: WorkflowRuleParams,
    val onSuccess: WorkflowStepId?,
    val onFailure: WorkflowStepId?,
    val startStep: Boolean
)

enum class WorkflowRuleType {
    INVOICE_AMOUNT,
    INVOICE_PROPERTY,
    SLACK_NOTIFICATION,
    EMAIL_NOTIFICATION
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
interface WorkflowRuleParams

data class InvoiceAmount(
    val lowerAmountLimit: Money?,
    val lowerAmountLimitInclusive: Boolean?,
    val upperAmountLimit: Money?,
    val upperAmountLimitInclusive: Boolean?
) : WorkflowRuleParams

data class Notification(
    val destination: String,
    val message: String
) : WorkflowRuleParams

data class InvoiceProperty(
    val name: PropertyName,
    val value: String
) : WorkflowRuleParams

enum class PropertyName {
    DEPARTMENT,
    APPROVAL_REQUIRED;


    fun getPropertyValueFrom(invoiceDao: InvoiceDao): String =
        when (this) {
            DEPARTMENT -> invoiceDao.department.name
            APPROVAL_REQUIRED -> invoiceDao.requitesApproval.toString()
        }
}