package org.light.challenge.logic.core.steps

import org.light.challenge.data.models.InvoiceDao

interface WorkflowStep {

    operator fun invoke(invoiceDao: InvoiceDao): Boolean
}