import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.TestInstance
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import org.light.challenge.data.*
import org.light.challenge.logic.core.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkflowServiceTest {

    private val db: Database = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    private fun createDb(){
        transaction(db) {
            SchemaUtils.create(EmployeesTable, RulesTable, WorkflowTable, InvoicesTable)
        }
    }

    private fun deleteDb(){
        transaction(db) {
            SchemaUtils.drop(EmployeesTable, RulesTable, WorkflowTable, InvoicesTable)
        }
    }
    @AfterAll
    fun cleanTest() {
        deleteDb()
        transaction(db){close()}
        File("./memory").delete()
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class TestAddRule{
        init{
            createDb()
        }

        private var index = 1
        @RepeatedTest(50)
        fun `test addRule return value`() {
            val rule = Rule(
                amountRange = Pair(100.0, null),
                department = Department.FINANCE,
                requiresManagerApproval = null,
                employeeUsername = "lsimon",
                contactMethod = ContactMethod.EMAIL
            )
            val expectedResult = index
            transaction(db){
                WorkflowService().addRule(rule)
                val lastRow = RulesTable.selectAll().orderBy(RulesTable.id to SortOrder.DESC).first()
                val result = lastRow[RulesTable.id]
                assertEquals(expectedResult, result)
            }
            ++index
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class TestProcessInvoice {
        init {
            deleteDb()
            createDb()
            createChallengeWorkflow()
        }
        private fun createChallengeWorkflow() {
            transaction(db) {
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "CMO",
                        contactMethod = ContactMethod.EMAIL,
                        amountRange = Pair(10000.0, null),
                        department = Department.MARKETING
                    )
                )
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "CFO",
                        contactMethod = ContactMethod.SLACK,
                        amountRange = Pair(10000.0, null),
                        department = Department.FINANCE
                    )
                )
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "Finance Manager",
                        contactMethod = ContactMethod.EMAIL,
                        amountRange = Pair(5000.0, 10000.0),
                        requiresManagerApproval = true
                    )
                )
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "Finance team Member",
                        contactMethod = ContactMethod.SLACK,
                        amountRange = Pair(5000.0, 10000.0),
                        requiresManagerApproval = false
                    )
                )
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "Finance team Member",
                        contactMethod = ContactMethod.SLACK,
                        amountRange = Pair(null, 5000.0)
                    )
                )
            }
        }
        @Test
        fun `test send EMAIL to CMO`() {

            transaction(db) {
            val expectedResult = "Send a message via EMAIL to CMO"
            val result = WorkflowService().processInvoice(Invoice(
                amount = 12000.0,
                department = Department.MARKETING,
                requiresManagerApproval = false
            ))
            assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send SLACK to CFO`() {

            transaction(db) {
                val expectedResult = "Send a message via SLACK to CFO"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 12000.0,
                    department = Department.FINANCE,
                    requiresManagerApproval = true
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send SLACK to finance Team Member 1`() {

            transaction(db) {
                val expectedResult = "Send a message via SLACK to Finance team Member"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 400.0,
                    department = Department.MARKETING,
                    requiresManagerApproval = false
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send SLACK to finance Team Member 2`() {

            transaction(db) {
                val expectedResult = "Send a message via SLACK to Finance team Member"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 7000.0,
                    department = Department.MARKETING,
                    requiresManagerApproval = false
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send EMAIL to finance Manager`() {

            transaction(db) {
                val expectedResult = "Send a message via EMAIL to Finance Manager"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 7000.0,
                    department = Department.FINANCE,
                    requiresManagerApproval = true
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test edge case invoice amount 10000`() {

            transaction(db) {
                val expectedResult = "Send a message via EMAIL to Finance Manager"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 10000.0,
                    department = Department.MARKETING,
                    requiresManagerApproval = true
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test edge case invoice amount 5000`() {

            transaction(db) {
                val expectedResult = "Send a message via SLACK to Finance team Member"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 5000.0,
                    department = Department.MARKETING,
                    requiresManagerApproval = true
                ))
                assertEquals(expectedResult, result)
            }
        }
    }
}