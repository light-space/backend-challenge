package org.light.challenge.logic.core.steps

import mu.KLogger
import mu.KotlinLogging
import org.javamoney.moneta.Money
import org.light.challenge.data.models.InvoiceDao

private val logger: KLogger = KotlinLogging.logger {}

internal class EvalInvoiceAmountStep(
    private val lowerAmountBound: Money? = null,
    private val isLowerBoundInclusive: Boolean? = null,
    private val upperAmountBound: Money? = null,
    private val isUpperBoundInclusive: Boolean? = null,
) : WorkflowStep {

    override fun invoke(invoiceDao: InvoiceDao): Boolean =
        (isLowerBoundCheckPassing(invoiceDao.amount) &&
            isUpperBoundCheckPassing(invoiceDao.amount))
            .also {
                logger.info {
                    "Executed EvalInvoiceAmountStep with " +
                        "lowerAmountBound '$lowerAmountBound', " +
                        "isLowerBoundInclusive '$isLowerBoundInclusive', " +
                        "upperAmountBound '$upperAmountBound', " +
                        "isUpperBoundInclusive '$isUpperBoundInclusive'"
                }
            }

    private fun isLowerBoundCheckPassing(amount: Money): Boolean =
        when {
            lowerAmountBound == null -> true
            isLowerBoundInclusive == true -> lowerAmountBound <= amount
            isLowerBoundInclusive == false || isLowerBoundInclusive == null -> lowerAmountBound < amount
            else -> true
        }

    private fun isUpperBoundCheckPassing(amount: Money): Boolean =
        when {
            upperAmountBound == null -> true
            isUpperBoundInclusive == true -> amount <= upperAmountBound
            isUpperBoundInclusive == false || isUpperBoundInclusive == null -> amount < upperAmountBound
            else -> true
        }
}