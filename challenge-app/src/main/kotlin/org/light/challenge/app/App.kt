package org.light.challenge.app

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mu.KLogger
import mu.KotlinLogging
import org.javamoney.moneta.Money
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.light.challenge.data.WorkflowStepRepository
import org.light.challenge.data.WorkflowTable
import org.light.challenge.data.models.CompanyId
import org.light.challenge.data.models.Department
import org.light.challenge.data.models.InvoiceAmount
import org.light.challenge.data.models.InvoiceDao
import org.light.challenge.data.models.InvoiceProperty
import org.light.challenge.data.models.Notification
import org.light.challenge.data.models.PropertyName
import org.light.challenge.data.models.WorkflowRule
import org.light.challenge.data.models.WorkflowRuleType
import org.light.challenge.data.models.WorkflowRuleType.EMAIL_NOTIFICATION
import org.light.challenge.data.models.WorkflowRuleType.SLACK_NOTIFICATION
import org.light.challenge.data.models.WorkflowStepDao
import org.light.challenge.data.models.WorkflowStepId
import org.light.challenge.logic.core.WorkflowBuilder
import org.light.challenge.logic.core.WorkflowService
import org.light.challenge.logic.core.WorkflowStepBinder
import org.light.challenge.logic.core.notifications.EmailNotificationIntegration
import org.light.challenge.logic.core.notifications.SlackNotificationIntegration
import org.zalando.jackson.datatype.money.MoneyModule
import java.io.File

private lateinit var workflowService: WorkflowService
private lateinit var workflowStepRepository: WorkflowStepRepository
private lateinit var workflowBuilder: WorkflowBuilder
private lateinit var workflowStepBinder: WorkflowStepBinder

private val emailNotificationIntegration = EmailNotificationIntegration()
private val slackNotificationIntegration = SlackNotificationIntegration()
private val objectMapper = ObjectMapper().apply {
    registerModules(
        KotlinModule.Builder().build(),
        MoneyModule().withAmountFieldName("value")
    )
}
private val logger: KLogger = KotlinLogging.logger {}

fun main(args: Array<String>) {

    // in-memory DB
    val db = Database.connect("jdbc:sqlite::memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")
    logger.info { "creating tables..." }
    createTables(db)

    logger.info { "creating bindings..." }
    createBindings(db)
    logger.info { "reading invoice to evaluate from file..." }
    val invoice = readInvoice(args.getOrNull(0))
    logger.info { "storing predefined workflow for invoice company '${invoice.companyId.id}'" }
    storeExampleWorkflow(args.getOrNull(1))
    //storeExampleWorkflow(invoice.companyId)

    logger.info { "running workflow..." }
    workflowService.startWorkflow(invoice)
    logger.info { "workflow finished" }
}

private fun createTables(db: Database) {
    transaction(db) {
        SchemaUtils.drop(WorkflowTable)
        SchemaUtils.create(WorkflowTable)
    }
}

private fun createBindings(db: Database) {

    workflowStepBinder = WorkflowStepBinder(
        sendEmailNotification = emailNotificationIntegration,
        sendSlackNotification = slackNotificationIntegration
    )

    workflowBuilder = WorkflowBuilder(workflowStepBinder = workflowStepBinder)

    workflowStepRepository = WorkflowStepRepository(db, objectMapper)

    workflowService = WorkflowService(
        workflowStepRepository = workflowStepRepository,
        workflowBuilder = workflowBuilder
    )
}

private fun readInvoice(path: String?): InvoiceDao =
    try {
        val content = File(path).readText()
        objectMapper.readValue(content, InvoiceDao::class.java)
    } catch (e: Exception) {
        logger.error(e) { "Error reading invoice file from args with path '$path'" }
        logger.info { "Will continue with file from resources" }
        val content = object {}.javaClass.getResource("/invoice.json").readText()
        objectMapper.readValue(content, InvoiceDao::class.java)
    }

private fun storeExampleWorkflow(path: String?) {
    try {
        val content = File(path).readText()
        objectMapper.readValue(content, object : TypeReference<List<WorkflowStepDao>>() {})
            .forEach { workflowStepRepository.save(it.workflowRule, it.id) }
    } catch (e: Exception) {
        logger.error(e) { "Error reading workflow file from args with path '$path'" }
        logger.info { "Will continue with file from resources" }
        val content = object {}.javaClass.getResource("/workflow.json").readText()

        objectMapper.readValue(content, object : TypeReference<List<WorkflowStepDao>>() {})
            .forEach { workflowStepRepository.save(it.workflowRule, it.id) }
    }
}

// Programmatic way to define workflow
private fun storeExampleWorkflow(companyId: CompanyId) {
    fun buildNotificationRule(
        notificationType: WorkflowRuleType,
        destination: String,
        message: String,
        startStep: Boolean = false,
        onSuccess: WorkflowStepId? = null,
        onFailure: WorkflowStepId? = null
    ) = WorkflowRule(
        companyId = companyId,
        type = notificationType,
        params = Notification(
            destination = destination,
            message = message
        ),
        onSuccess = onSuccess,
        onFailure = onFailure,
        startStep = startStep
    )

    fun buildPropertyRule(
        propertyName: PropertyName,
        propertyValue: String,
        startStep: Boolean = false,
        onSuccess: WorkflowStepId? = null,
        onFailure: WorkflowStepId? = null
    ) = WorkflowRule(
        companyId = companyId,
        type = WorkflowRuleType.INVOICE_PROPERTY,
        params = InvoiceProperty(
            name = propertyName,
            value = propertyValue
        ),
        onSuccess = onSuccess,
        onFailure = onFailure,
        startStep = startStep
    )

    fun buildAmountRule(
        lowerAmountLimit: Money,
        startStep: Boolean = false,
        onSuccess: WorkflowStepId? = null,
        onFailure: WorkflowStepId? = null
    ) = WorkflowRule(
        companyId = companyId,
        type = WorkflowRuleType.INVOICE_AMOUNT,
        params = InvoiceAmount(
            lowerAmountLimit = lowerAmountLimit,
            lowerAmountLimitInclusive = false,
            null,
            null
        ),
        onSuccess = onSuccess,
        onFailure = onFailure,
        startStep = startStep
    )

    val emailNotificationToCmo = buildNotificationRule(EMAIL_NOTIFICATION, "CMO", "Please approve this invoice")
        .let(workflowStepRepository::save)
        .also { workflowDao -> logger.info { objectMapper.writeValueAsString(workflowDao) } }

    val emailNotificationToFinanceManager =
        buildNotificationRule(EMAIL_NOTIFICATION, "FinanceManager", "Please approve this invoice")
            .let(workflowStepRepository::save)
            .also { workflowDao -> logger.info { objectMapper.writeValueAsString(workflowDao) } }

    val slackNotificationToCfo = buildNotificationRule(SLACK_NOTIFICATION, "CFO", "Please approve this invoice")
        .let(workflowStepRepository::save)
        .also { workflowDao -> logger.info { objectMapper.writeValueAsString(workflowDao) } }

    val slackNotificationToFinanceTeamMember =
        buildNotificationRule(SLACK_NOTIFICATION, "FinanceTeamMember", "Please approve this invoice")
            .let(workflowStepRepository::save)
            .also { workflowDao -> logger.info { objectMapper.writeValueAsString(workflowDao) } }

    val invoiceRelatedToMarketing =
        buildPropertyRule(
            propertyName = PropertyName.DEPARTMENT,
            propertyValue = Department.MARKETING.name,
            onSuccess = emailNotificationToCmo.id,
            onFailure = slackNotificationToCfo.id
        )
            .let(workflowStepRepository::save)
            .also { workflowDao -> logger.info { objectMapper.writeValueAsString(workflowDao) } }

    val invoiceRequiresApproval =
        buildPropertyRule(
            propertyName = PropertyName.APPROVAL_REQUIRED,
            propertyValue = true.toString(),
            onSuccess = emailNotificationToFinanceManager.id,
            onFailure = slackNotificationToFinanceTeamMember.id
        )
            .let(workflowStepRepository::save)
            .also { workflowDao -> logger.info { objectMapper.writeValueAsString(workflowDao) } }

    val amountBiggerThan5k =
        buildAmountRule(
            Money.of(5000, "USD"),
            onSuccess = invoiceRequiresApproval.id,
            onFailure = slackNotificationToFinanceTeamMember.id
        )
            .let(workflowStepRepository::save)
            .also { workflowDao -> logger.info { objectMapper.writeValueAsString(workflowDao) } }

    // val amountBiggerThan10k =
    buildAmountRule(
        Money.of(10000, "USD"),
        startStep = true,
        onSuccess = invoiceRelatedToMarketing.id,
        onFailure = amountBiggerThan5k.id
    )
        .let(workflowStepRepository::save)
        .also { workflowDao -> logger.info { objectMapper.writeValueAsString(workflowDao) } }
}

