package org.light.challenge.logic.core.fixtures

import org.javamoney.moneta.Money
import org.light.challenge.data.models.CompanyId
import org.light.challenge.data.models.Department
import org.light.challenge.data.models.InvoiceAmount
import org.light.challenge.data.models.InvoiceProperty
import org.light.challenge.data.models.Notification
import org.light.challenge.data.models.PropertyName
import org.light.challenge.data.models.WorkflowRule
import org.light.challenge.data.models.WorkflowRuleType
import org.light.challenge.data.models.WorkflowStepDao
import org.light.challenge.data.models.WorkflowStepId
import java.util.UUID

val emailNotificationToCmo =
    buildNotificationWorkflowStepDto(WorkflowRuleType.EMAIL_NOTIFICATION, "CMO", "Please approve this invoice")
val emailNotificationToFinanceManager = buildNotificationWorkflowStepDto(
    WorkflowRuleType.EMAIL_NOTIFICATION,
    "FinanceManager",
    "Please approve this invoice"
)
val slackNotificationToCfo =
    buildNotificationWorkflowStepDto(WorkflowRuleType.SLACK_NOTIFICATION, "CFO", "Please approve this invoice")
val slackNotificationToFinanceTeamMember = buildNotificationWorkflowStepDto(
    WorkflowRuleType.SLACK_NOTIFICATION,
    "FinanceTeamMember",
    "Please approve this invoice"
)
val invoiceRelatedToMarketing = buildPropertyWorkflowStepDto(
    propertyName = PropertyName.DEPARTMENT,
    propertyValue = Department.MARKETING.name,
    onSuccess = emailNotificationToCmo.id,
    onFailure = slackNotificationToCfo.id
)
val invoiceRequiresApproval = buildPropertyWorkflowStepDto(
    propertyName = PropertyName.APPROVAL_REQUIRED,
    propertyValue = true.toString(),
    onSuccess = emailNotificationToFinanceManager.id,
    onFailure = slackNotificationToFinanceTeamMember.id
)
val invoiceRelatedToFinance = buildPropertyWorkflowStepDto(
    propertyName = PropertyName.DEPARTMENT,
    propertyValue = Department.FINANCE.name
)
val amountBiggerThan5k = buildAmountWorkflowStepDto(
    Money.of(5000, "USD"),
    onSuccess = invoiceRequiresApproval.id,
    onFailure = slackNotificationToFinanceTeamMember.id
)
val amountBiggerThan10k = buildAmountWorkflowStepDto(
    Money.of(10000, "USD"),
    startStep = true,
    onSuccess = invoiceRelatedToMarketing.id,
    onFailure = amountBiggerThan5k.id
)

val completeWorkflowSteps = setOf(
    emailNotificationToCmo,
    emailNotificationToFinanceManager,
    slackNotificationToCfo,
    slackNotificationToFinanceTeamMember,
    invoiceRelatedToMarketing,
    invoiceRequiresApproval,
    amountBiggerThan5k,
    amountBiggerThan10k
)

private fun buildAmountWorkflowStepDto(
    lowerAmountLimit: Money,
    startStep: Boolean = false,
    onSuccess: WorkflowStepId? = null,
    onFailure: WorkflowStepId? = null
) =
    WorkflowStepId(UUID.randomUUID()).let { workflowStepId ->
        WorkflowStepDao(
            id = workflowStepId,
            workflowRule = WorkflowRule(
                companyId = CompanyId(UUID.randomUUID()),
                type = WorkflowRuleType.INVOICE_AMOUNT,
                params = InvoiceAmount(
                    lowerAmountLimit = lowerAmountLimit,
                    lowerAmountLimitInclusive = false,
                    null,
                    null
                ),
                onSuccess = onSuccess,
                onFailure = onFailure,
                startStep = startStep
            )
        )
    }

private fun buildPropertyWorkflowStepDto(
    propertyName: PropertyName,
    propertyValue: String,
    startStep: Boolean = false,
    onSuccess: WorkflowStepId? = null,
    onFailure: WorkflowStepId? = null
) =
    WorkflowStepId(UUID.randomUUID()).let { workflowStepId ->
        WorkflowStepDao(
            id = workflowStepId,
            workflowRule = WorkflowRule(
                companyId = CompanyId(UUID.randomUUID()),
                type = WorkflowRuleType.INVOICE_PROPERTY,
                params = InvoiceProperty(
                    name = propertyName,
                    value = propertyValue
                ),
                onSuccess = onSuccess,
                onFailure = onFailure,
                startStep = startStep
            )
        )
    }

private fun buildNotificationWorkflowStepDto(
    notificationType: WorkflowRuleType,
    destination: String,
    message: String,
    startStep: Boolean = false,
    onSuccess: WorkflowStepId? = null,
    onFailure: WorkflowStepId? = null
) =
    WorkflowStepId(UUID.randomUUID()).let { workflowStepId ->
        WorkflowStepDao(
            id = workflowStepId,
            workflowRule = WorkflowRule(
                companyId = CompanyId(UUID.randomUUID()),
                type = notificationType,
                params = Notification(
                    destination = destination,
                    message = message
                ),
                onSuccess = onSuccess,
                onFailure = onFailure,
                startStep = startStep
            )
        )
    }