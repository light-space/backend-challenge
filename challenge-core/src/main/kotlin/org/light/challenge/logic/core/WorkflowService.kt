package org.light.challenge.logic.core

import mu.KLogger
import mu.KotlinLogging
import org.light.challenge.data.WorkflowStepRepository
import org.light.challenge.data.models.InvoiceDao

private val logger: KLogger = KotlinLogging.logger {}

class WorkflowService(
    private val workflowStepRepository: WorkflowStepRepository,
    private val workflowBuilder: WorkflowBuilder
) {
    fun startWorkflow(invoiceDao: InvoiceDao) {
        workflowStepRepository.getBy(invoiceDao.companyId)
            .also { if (it.isEmpty()) logger.error { "There is no workflow defined for company ${invoiceDao.companyId.id}" } }
            .let(workflowBuilder::invoke)
            .invoke(invoiceDao)
    }
}
