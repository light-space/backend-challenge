package org.light.challenge.logic.core

import io.mockk.every
import io.mockk.mockk
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test
import org.light.challenge.data.models.InvoiceAmount
import org.light.challenge.data.models.InvoiceProperty
import org.light.challenge.data.models.PropertyName
import org.light.challenge.data.models.WorkflowRuleType
import org.light.challenge.data.models.WorkflowStepDao
import org.light.challenge.logic.core.notifications.NotificationIntegration
import org.light.challenge.logic.core.steps.EvalInvoiceAmountStep
import org.light.challenge.logic.core.steps.EvalInvoicePropertyStep

internal class WorkflowStepBinderTest {

    private val sendEmailNotification = mockk<NotificationIntegration>()
    private val sendSlackNotification = mockk<NotificationIntegration>()

    private val workflowStepBinder = WorkflowStepBinder(
        sendEmailNotification = sendEmailNotification,
        sendSlackNotification = sendSlackNotification
    )

    @Test
    fun `should bind INVOICE_AMOUNT WorkflowRuleType`() {
        // Given
        val lowerAmountLimitAnswer = Money.of(10, "USD")
        val workflowStepDao = mockk<WorkflowStepDao> {
            every { type() } returns WorkflowRuleType.INVOICE_AMOUNT
            every { params() } returns mockk<InvoiceAmount> {
                every { lowerAmountLimit } returns lowerAmountLimitAnswer
                every { lowerAmountLimitInclusive } returns null
                every { upperAmountLimit } returns null
                every { upperAmountLimitInclusive } returns null
            }
        }

        // When
        val result = workflowStepBinder.bind(workflowStepDao)

        // Then
        assert(result is EvalInvoiceAmountStep)
        // also assert values
    }

    @Test
    fun `should bind INVOICE_PROPERTY WorkflowRuleType`() {
        // Given
        val workflowStepDao = mockk<WorkflowStepDao> {
            every { type() } returns WorkflowRuleType.INVOICE_PROPERTY
            every { params() } returns mockk<InvoiceProperty> {
                every { name } returns PropertyName.DEPARTMENT
                every { value } returns ""
            }
        }

        // When
        val result = workflowStepBinder.bind(workflowStepDao)

        // Then
        assert(result is EvalInvoicePropertyStep)
        // also assert values
    }

    // @Test
    fun `should bind SLACK_NOTIFICATION WorkflowRuleType`() {
        TODO()
    }

    // @Test
    fun `should bind EMAIL_NOTIFICATION WorkflowRuleType`() {
        TODO()
    }
}