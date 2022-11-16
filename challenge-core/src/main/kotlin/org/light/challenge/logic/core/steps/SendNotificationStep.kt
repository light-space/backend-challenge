package org.light.challenge.logic.core.steps

import mu.KotlinLogging
import mu.withLoggingContext
import org.light.challenge.data.models.InvoiceDao

private val logger = KotlinLogging.logger {}

internal class SendNotificationStep(
    private val destination: String,
    private val message: String,
    private val sendNotification: (String, String) -> Unit
) : WorkflowStep {

    override fun invoke(invoiceDao: InvoiceDao): Boolean =
        withLoggingContext(
            "invoiceId" to invoiceDao.id.toString(),
            "destination" to destination,
        ) {
            try {
                logger.info { "Sending notification to '$destination'" }
                // Add invoice information to the message
                val resultingMessage = message // + invoice information
                sendNotification(destination, resultingMessage)
                true
            } catch (e: Exception) {
                logger.error(e) { "Failed on sending notification for invoice '${invoiceDao.id}'" }
                false
            }
        }
}