package org.light.challenge.logic.core.notifications

interface NotificationIntegration {

    operator fun invoke(destination: String, message: String)
}