package org.light.challenge.logic.core

import org.light.challenge.data.models.InvoiceAmount
import org.light.challenge.data.models.Notification
import org.light.challenge.data.models.InvoiceProperty
import org.light.challenge.data.models.WorkflowStepDao
import org.light.challenge.data.models.WorkflowRuleType
import org.light.challenge.logic.core.notifications.NotificationIntegration
import org.light.challenge.logic.core.steps.EvalInvoiceAmountStep
import org.light.challenge.logic.core.steps.EvalInvoicePropertyStep
import org.light.challenge.logic.core.steps.SendNotificationStep
import org.light.challenge.logic.core.steps.WorkflowStep

class WorkflowStepBinder(
    // bind to EmailNotificationIntegration
    private val sendEmailNotification: NotificationIntegration,
    // bind to SlackNotificationIntegration
    private val sendSlackNotification: NotificationIntegration
) {

    fun bind(workflowStepDao: WorkflowStepDao): WorkflowStep =
        when (workflowStepDao.type()) {
            WorkflowRuleType.INVOICE_AMOUNT -> bindInvoiceAmountStep(workflowStepDao)
            WorkflowRuleType.INVOICE_PROPERTY -> bindInvoicePropertyStep(workflowStepDao)
            WorkflowRuleType.SLACK_NOTIFICATION -> bindNotificationStep(workflowStepDao) { destination, message ->
                sendSlackNotification(destination, message)
            }

            WorkflowRuleType.EMAIL_NOTIFICATION -> bindNotificationStep(workflowStepDao) { destination, message ->
                sendEmailNotification(destination, message)
            }
        }

    private fun bindNotificationStep(workflowStepDao: WorkflowStepDao, sendNotification: (String, String) -> Unit) =
        with(workflowStepDao.params() as Notification) {
            SendNotificationStep(
                destination = destination,
                message = message,
                sendNotification = sendNotification
            )
        }

    private fun bindInvoiceAmountStep(workflowStepDao: WorkflowStepDao) =
        with(workflowStepDao.params() as InvoiceAmount) {
            EvalInvoiceAmountStep(
                lowerAmountBound = lowerAmountLimit,
                isLowerBoundInclusive = lowerAmountLimitInclusive,
                upperAmountBound = upperAmountLimit,
                isUpperBoundInclusive = upperAmountLimitInclusive,
            )
        }

    private fun bindInvoicePropertyStep(workflowStepDao: WorkflowStepDao) =
        with(workflowStepDao.params() as InvoiceProperty) {
            EvalInvoicePropertyStep(target = value) { invoice ->
                name.getPropertyValueFrom(invoice)
            }
        }
}