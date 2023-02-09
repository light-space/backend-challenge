import org.junit.jupiter.api.Assertions.assertEquals
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.io.File
import org.light.challenge.data.*
import org.light.challenge.logic.core.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkflowServiceTest {

    private val db: Database = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")

    private fun createDb(){
        transaction(db) {
            SchemaUtils.create(EmployeesTable, WorkflowTable, InvoicesTable)
        }
    }

    private fun deleteDb(){
        transaction(db) {
            SchemaUtils.drop(EmployeesTable, WorkflowTable, InvoicesTable)
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
                val lastRow = WorkflowTable.selectAll().orderBy(WorkflowTable.id to SortOrder.DESC).first()
                val result = lastRow[WorkflowTable.id]
                assertEquals(expectedResult, result)
            }
            ++index
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class TestProcessInvoice {
        private fun resetDB(){
            deleteDb()
            createDb()
            createChallengeWorkflow()
            transaction(db){
                WorkflowService().generateEmployeeTable()
            }
        }
        init {
            resetDB()
        }

        @BeforeEach
        fun reset(){resetDB()}

        @AfterAll
        fun deleteEmployeeTable() {
            transaction(db){
                WorkflowService().deleteEmployeeTable()
            }
        }
        private fun createChallengeWorkflow() {
            transaction(db) {
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "jsanders",
                        contactMethod = ContactMethod.EMAIL,
                        amountRange = Pair(10000.0, null),
                        department = Department.MARKETING
                    )
                )
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "fkozjak",
                        contactMethod = ContactMethod.SLACK,
                        amountRange = Pair(10000.0, null),
                        department = Department.FINANCE
                    )
                )
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "jcop",
                        contactMethod = ContactMethod.EMAIL,
                        amountRange = Pair(5000.0, 10000.0),
                        requiresManagerApproval = true
                    )
                )
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "lsimon",
                        contactMethod = ContactMethod.SLACK,
                        amountRange = Pair(5000.0, 10000.0),
                        requiresManagerApproval = false
                    )
                )
                WorkflowService().addRule(
                    Rule(
                        employeeUsername = "meetsoon",
                        contactMethod = ContactMethod.SLACK,
                        amountRange = Pair(null, 5000.0)
                    )
                )
            }
        }

        @Test
        fun `test send EMAIL to CMO`() {

            transaction(db) {
            val expectedResult =
                "EMAIL, Sending approval request for invoice #1 to Jonathan Sanders.\n" +
                "Email: jonathan@light.inc\nRole: CMO"
            val result = WorkflowService().processInvoice(Invoice(
                amount = 12000.0,
                department = Department.MARKETING,
                requiresManagerApproval = false,
                approved = false,
                approverUsername = "jsanders"
            ))
            assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send SLACK to CFO`() {

            transaction(db) {
                val expectedResult =
                    "SLACK, Sending approval request for invoice #1 to Filip Kozjak.\n" +
                    "Slack user: filip\nRole: CFO"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 12000.0,
                    department = Department.FINANCE,
                    requiresManagerApproval = true,
                    approved = false,
                    approverUsername = "fkozjak"
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send SLACK to finance Team Member 1`() {

            transaction(db) {
                val expectedResult =
                    "SLACK, Sending approval request for invoice #1 to Meet Soon.\n" +
                    "Slack user: meetsoon\nRole: Finance Member"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 400.0,
                    department = Department.MARKETING,
                    requiresManagerApproval = false,
                    approved = false,
                    approverUsername = "meetsoon"
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send SLACK to finance Team Member 2`() {

            transaction(db) {
                val expectedResult =
                    "SLACK, Sending approval request for invoice #1 to Lluis Simon.\n" +
                    "Slack user: lluis.simon.92\nRole: Finance Member"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 7000.0,
                    department = Department.MARKETING,
                    requiresManagerApproval = false,
                    approved = false,
                    approverUsername = "lsimon"
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test send EMAIL to finance Manager`() {

            transaction(db) {
                val expectedResult =
                    "EMAIL, Sending approval request for invoice #1 to Jelena Cop.\n" +
                    "Email: jelena@light.inc\nRole: Finance Manager"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 7000.0,
                    department = Department.FINANCE,
                    requiresManagerApproval = true,
                    approved = false,
                    approverUsername = "jcop"
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test edge case invoice amount 10000`() {

            transaction(db) {
                val expectedResult =
                    "EMAIL, Sending approval request for invoice #1 to Jelena Cop.\n" +
                    "Email: jelena@light.inc\nRole: Finance Manager"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 10000.0,
                    department = Department.MARKETING,
                    requiresManagerApproval = true,
                    approved = false,
                    approverUsername = "jcop"
                ))
                assertEquals(expectedResult, result)
            }
        }

        @Test
        fun `test edge case invoice amount 5000`() {

            transaction(db) {
                val expectedResult =
                    "SLACK, Sending approval request for invoice #1 to Meet Soon.\n" +
                    "Slack user: meetsoon\nRole: Finance Member"
                val result = WorkflowService().processInvoice(Invoice(
                    amount = 5000.0,
                    department = Department.MARKETING,
                    requiresManagerApproval = true,
                    approved = false,
                    approverUsername = "meetsoon"
                ))
                assertEquals(expectedResult, result)
            }
        }
    }
}