import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.light.challenge.logic.core.TypeHelper
import org.light.challenge.logic.core.WorkflowService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.TestInstance
import org.light.challenge.data.EmployeesTable
import org.light.challenge.data.RulesTable
import org.light.challenge.data.WorkflowTable
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkflowServiceTest {

    private val db: Database = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    private fun createDb(){
        transaction(db) {
            SchemaUtils.create(EmployeesTable, RulesTable, WorkflowTable)
        }
    }

    private fun deleteDb(){
        transaction(db) {
            SchemaUtils.drop(EmployeesTable, RulesTable, WorkflowTable)
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
            val rule = TypeHelper.Rule(
                amountRange = Pair(100.0, null),
                department = TypeHelper.Department.FINANCE,
                requiresManagerApproval = null,
                employeeUsername = "lsimon",
                contactMethod = TypeHelper.ContactMethod.EMAIL
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
                    TypeHelper.Rule(
                        employeeUsername = "CMO",
                        contactMethod = TypeHelper.ContactMethod.EMAIL,
                        amountRange = Pair(10000.0, null),
                        department = TypeHelper.Department.MARKETING
                    )
                )
                WorkflowService().addRule(
                    TypeHelper.Rule(
                        employeeUsername = "CFO",
                        contactMethod = TypeHelper.ContactMethod.SLACK,
                        amountRange = Pair(10000.0, null),
                        department = TypeHelper.Department.FINANCE
                    )
                )
                WorkflowService().addRule(
                    TypeHelper.Rule(
                        employeeUsername = "Finance Manager",
                        contactMethod = TypeHelper.ContactMethod.EMAIL,
                        amountRange = Pair(5000.0, 10000.0),
                        requiresManagerApproval = true
                    )
                )
                WorkflowService().addRule(
                    TypeHelper.Rule(
                        employeeUsername = "Finance team Member",
                        contactMethod = TypeHelper.ContactMethod.SLACK,
                        amountRange = Pair(5000.0, 10000.0),
                        requiresManagerApproval = false
                    )
                )
                WorkflowService().addRule(
                    TypeHelper.Rule(
                        employeeUsername = "Finance team Member",
                        contactMethod = TypeHelper.ContactMethod.SLACK,
                        amountRange = Pair(null, 5000.0)
                    )
                )
            }
        }
        @Test
        fun `test send EMAIL to CMO`() {

            transaction(db) {
            val expectedResult = "Send a message via EMAIL to CMO"
            val result = WorkflowService().processInvoice(TypeHelper.Invoice(
                amount = 12000.0,
                department = TypeHelper.Department.MARKETING,
                requiresManagerApproval = false
            ))
            assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send SLACK to CFO`() {

            transaction(db) {
                val expectedResult = "Send a message via SLACK to CFO"
                val result = WorkflowService().processInvoice(TypeHelper.Invoice(
                    amount = 12000.0,
                    department = TypeHelper.Department.FINANCE,
                    requiresManagerApproval = true
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send SLACK to finance Team Member 1`() {

            transaction(db) {
                val expectedResult = "Send a message via SLACK to Finance team Member"
                val result = WorkflowService().processInvoice(TypeHelper.Invoice(
                    amount = 400.0,
                    department = TypeHelper.Department.MARKETING,
                    requiresManagerApproval = false
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send SLACK to finance Team Member 2`() {

            transaction(db) {
                val expectedResult = "Send a message via SLACK to Finance team Member"
                val result = WorkflowService().processInvoice(TypeHelper.Invoice(
                    amount = 7000.0,
                    department = TypeHelper.Department.MARKETING,
                    requiresManagerApproval = false
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send EMAIL to finance Manager`() {

            transaction(db) {
                val expectedResult = "Send a message via EMAIL to Finance Manager"
                val result = WorkflowService().processInvoice(TypeHelper.Invoice(
                    amount = 7000.0,
                    department = TypeHelper.Department.FINANCE,
                    requiresManagerApproval = true
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test edge case invoice amount 10000`() {

            transaction(db) {
                val expectedResult = "Send a message via EMAIL to Finance Manager"
                val result = WorkflowService().processInvoice(TypeHelper.Invoice(
                    amount = 10000.0,
                    department = TypeHelper.Department.MARKETING,
                    requiresManagerApproval = true
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test edge case invoice amount 5000`() {

            transaction(db) {
                val expectedResult = "Send a message via SLACK to Finance team Member"
                val result = WorkflowService().processInvoice(TypeHelper.Invoice(
                    amount = 5000.0,
                    department = TypeHelper.Department.MARKETING,
                    requiresManagerApproval = true
                ))
                assertEquals(expectedResult, result)
            }
        }
    }
}