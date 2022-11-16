package org.light.challenge.data

import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.light.challenge.data.models.CompanyId
import org.light.challenge.data.models.WorkflowRule
import org.light.challenge.data.models.WorkflowRuleParams
import org.light.challenge.data.models.WorkflowStepDao
import org.light.challenge.data.models.WorkflowStepId
import java.util.UUID

class WorkflowStepRepository(
    val db: Database,
    val mapper: ObjectMapper
) {

    private fun <T> tx(statement: Transaction.() -> T): T = transaction(db) {
        this.statement()
    }

    fun getBy(companyId: CompanyId): Set<WorkflowStepDao> =
        tx {
            WorkflowTable.select(WorkflowTable.companyId eq companyId.id.toString())
                .map { it.toWorkflowStepDto(mapper) }
                .toSet()
        }

    fun save(workflowRule: WorkflowRule, id: WorkflowStepId? = null): WorkflowStepDao {
        val insertId = id?.id ?: UUID.randomUUID()
        tx {
            WorkflowTable.insert {
                it[this.id] = EntityID(insertId.toString(), WorkflowTable)
                it[this.companyId] = workflowRule.companyId.id.toString()
                it[this.workflowType] = workflowRule.type
                it[this.params] = mapper.writeValueAsString(workflowRule.params)
                it[this.onSuccess] = workflowRule.onSuccess?.id?.toString()
                it[this.onFailure] = workflowRule.onFailure?.id?.toString()
                it[this.startStep] = workflowRule.startStep
            }
        }
        return WorkflowStepDao(WorkflowStepId(insertId), workflowRule)
    }
}

private fun ResultRow.toWorkflowStepDto(mapper: ObjectMapper) =
    WorkflowStepDao(
        id = WorkflowStepId(UUID.fromString(this[WorkflowTable.id].value)),
        workflowRule = WorkflowRule(
            companyId = CompanyId(UUID.fromString(this[WorkflowTable.companyId])),
            type = this[WorkflowTable.workflowType],
            params = mapper.readValue(this[WorkflowTable.params], WorkflowRuleParams::class.java),
            onSuccess = this[WorkflowTable.onSuccess]?.let { WorkflowStepId(UUID.fromString(it)) },
            onFailure = this[WorkflowTable.onFailure]?.let { WorkflowStepId(UUID.fromString(it)) },
            startStep = this[WorkflowTable.startStep]
        )
    )



