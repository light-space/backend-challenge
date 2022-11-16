package org.light.challenge.data

import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Column
import org.light.challenge.data.models.WorkflowRuleType

object WorkflowTable : IdTable<String>(name = "workflow") {
    override val id: Column<EntityID<String>> = varchar("id", 255).entityId()
    val companyId = varchar("companyId", 255)
    val workflowType = enumerationByName("workflow_type", 255, WorkflowRuleType::class)
    val params = text("params")
    val onSuccess = varchar("on_success", 255).nullable()
    val onFailure = varchar("on_failure", 255).nullable()
    val startStep = bool("start_step")
}