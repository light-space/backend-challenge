package org.light.challenge.logic.core

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.light.challenge.logic.core.fixtures.amountBiggerThan10k
import org.light.challenge.logic.core.fixtures.amountBiggerThan5k
import org.light.challenge.logic.core.fixtures.completeWorkflowSteps
import org.light.challenge.logic.core.fixtures.emailNotificationToCmo
import org.light.challenge.logic.core.fixtures.emailNotificationToFinanceManager
import org.light.challenge.logic.core.fixtures.invoiceRelatedToFinance
import org.light.challenge.logic.core.fixtures.invoiceRelatedToMarketing
import org.light.challenge.logic.core.fixtures.invoiceRequiresApproval
import org.light.challenge.logic.core.fixtures.slackNotificationToCfo
import org.light.challenge.logic.core.fixtures.slackNotificationToFinanceTeamMember
import org.light.challenge.logic.core.steps.WorkflowStep
import kotlin.reflect.jvm.isAccessible

internal class WorkflowBuilderTest {
    // I only need them to be accessible for testing purposes,
    // that is why I decided to use reflexion instead of making them public
    private val currentStep =
        Workflow::class.members.find { it.name == "currentStep" }!!.apply { isAccessible = true }
    private val onCurrentStepSuccess =
        Workflow::class.members.find { it.name == "onCurrentStepSuccess" }!!.apply { isAccessible = true }
    private val onCurrentStepFailure =
        Workflow::class.members.find { it.name == "onCurrentStepFailure" }!!.apply { isAccessible = true }

    private val amountBiggerThan5kWorkflowStep = mockk<WorkflowStep>()
    private val invoiceRelatedToMarketingWorkflowStep = mockk<WorkflowStep>()
    private val invoiceRequiresApprovalWorkflowStep = mockk<WorkflowStep>()
    private val workflowStepBinder = mockk<WorkflowStepBinder>() {
        every { bind(emailNotificationToCmo) } returns mockk()
        every { bind(emailNotificationToFinanceManager) } returns mockk()
        every { bind(slackNotificationToCfo) } returns mockk()
        every { bind(slackNotificationToFinanceTeamMember) } returns mockk()
        every { bind(invoiceRelatedToMarketing) } returns invoiceRelatedToMarketingWorkflowStep
        every { bind(invoiceRequiresApproval) } returns invoiceRequiresApprovalWorkflowStep
        every { bind(amountBiggerThan5k) } returns amountBiggerThan5kWorkflowStep
        every { bind(amountBiggerThan10k) } returns mockk()
    }
    private val workflowBuilder = WorkflowBuilder(workflowStepBinder)

    @Test
    fun `should build workflow from a set of steps`() {
        // Given
        val workflowSteps = completeWorkflowSteps

        // When
        val result = workflowBuilder.invoke(workflowSteps)

        // Then
        assert(currentStep.call(onCurrentStepSuccess.call(result)) == invoiceRelatedToMarketingWorkflowStep)
        assert(currentStep.call(onCurrentStepFailure.call(result)) == amountBiggerThan5kWorkflowStep)
        assert(
            currentStep.call(
                onCurrentStepSuccess.call(
                    onCurrentStepFailure.call(result)
                )
            ) == invoiceRequiresApprovalWorkflowStep
        )
        // ...
        // in real life I would verify the full list
    }

    @Test
    fun `should ignore workflow step not linked to any other`() {
        // Given
        val workflowSteps = completeWorkflowSteps.plus(invoiceRelatedToFinance)

        // When
        workflowBuilder.invoke(workflowSteps)

        // Then
        verify(exactly = 0) { workflowStepBinder.bind(invoiceRelatedToFinance) }
    }

    // should catch and throw a business Exception for this case
    @Test
    fun `should throw StackOverflowError when there is a cyclic reference`() {
        // Given
        var workflowSteps = completeWorkflowSteps.minus(invoiceRequiresApproval)
        val invoiceRequiresApproval = invoiceRequiresApproval.copy(
            workflowRule = invoiceRequiresApproval.workflowRule.copy(
                onSuccess = amountBiggerThan10k.id
            )
        )
        workflowSteps = workflowSteps.plus(invoiceRequiresApproval)
        every { workflowStepBinder.bind(invoiceRequiresApproval) } returns mockk()

        // When / Then
        assertThrows<StackOverflowError> {
            workflowBuilder.invoke(workflowSteps)
        }
    }

    // should catch and throw a business Exception for this case
    @Test
    fun `should throw NullPointerException when one of the steps is missing`() {
        // Given
        val workflowSteps = completeWorkflowSteps.minus(invoiceRequiresApproval)

        // When / Then
        assertThrows<NullPointerException> {
            workflowBuilder.invoke(workflowSteps)
        }
    }
}