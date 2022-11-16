package org.light.challenge.logic.core.notifications

import mu.KLogger
import mu.KotlinLogging
import mu.withLoggingContext

class SlackNotificationIntegration(
    private val logger: KLogger = KotlinLogging.logger {}
) : NotificationIntegration {
    override fun invoke(destination: String, message: String) {
        withLoggingContext(
            "slackUser" to destination,
            "message" to message
        ) {
            logger.info { "Sent slack notification to approve Invoice" }
        }
    }
}