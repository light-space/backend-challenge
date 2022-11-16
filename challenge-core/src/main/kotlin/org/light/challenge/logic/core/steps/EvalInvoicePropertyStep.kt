package org.light.challenge.logic.core.steps

import mu.KLogger
import mu.KotlinLogging
import org.light.challenge.data.models.InvoiceDao

private val logger: KLogger = KotlinLogging.logger {}

internal class EvalInvoicePropertyStep(
    private val target: String,
    private val getSource: (InvoiceDao) -> String
) : WorkflowStep {

    override fun invoke(invoiceDao: InvoiceDao): Boolean =
        getSource(invoiceDao) == target
            .also {
                logger.info { "Executed EvalInvoicePropertyStep with target '$target'" }
            }
}