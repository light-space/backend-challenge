package org.light.challenge.logic.core.notifications

import mu.KLogger
import mu.KotlinLogging
import mu.withLoggingContext

class EmailNotificationIntegration(
    private val logger: KLogger = KotlinLogging.logger {}
) : NotificationIntegration {

    override fun invoke(destination: String, message: String) {
        withLoggingContext(
            "emailAddress" to destination,
            "message" to message
        ) {
            logger.info { "Sent email notification to approve Invoice" }
        }
    }
}