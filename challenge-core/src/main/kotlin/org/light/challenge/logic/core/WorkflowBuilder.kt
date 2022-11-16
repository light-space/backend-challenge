package org.light.challenge.logic.core

import mu.KLogger
import mu.KotlinLogging
import org.light.challenge.data.models.InvoiceDao
import org.light.challenge.data.models.WorkflowStepDao
import org.light.challenge.data.models.WorkflowStepId
import org.light.challenge.logic.core.steps.WorkflowStep

// TODO: Too much recursion, should be improve
class WorkflowBuilder(
    private val workflowStepBinder: WorkflowStepBinder
) {
    operator fun invoke(workflowSteps: Set<WorkflowStepDao>): Workflow {
        val stepsDictionary = workflowSteps.associateBy { it.id }
        val firstStep = workflowSteps.single(WorkflowStepDao::startStep)
        return buildWorkflow(stepsDictionary, firstStep.id)!!
    }

    private fun buildWorkflow(
        stepsDictionary: Map<WorkflowStepId, WorkflowStepDao>,
        workflowStepId: WorkflowStepId?
    ): Workflow? {
        if (workflowStepId == null) return null

        val workflowStepDto = stepsDictionary[workflowStepId]!!
        return Workflow(
            currentStep = workflowStepBinder.bind(workflowStepDto),
            onCurrentStepSuccess = buildWorkflow(stepsDictionary, workflowStepDto.onSuccess()),
            onCurrentStepFailure = buildWorkflow(stepsDictionary, workflowStepDto.onFailure())
        )
    }
}

class Workflow(
    private val currentStep: WorkflowStep,
    private val onCurrentStepSuccess: Workflow?,
    private val onCurrentStepFailure: Workflow?
) {
    operator fun invoke(invoiceDao: InvoiceDao) {
        runWorkflow(invoiceDao)
    }

    private fun runWorkflow(invoiceDao: InvoiceDao) {
        if (currentStep(invoiceDao)) onCurrentStepSuccess?.invoke(invoiceDao)
        else onCurrentStepFailure?.invoke(invoiceDao)
    }
}
