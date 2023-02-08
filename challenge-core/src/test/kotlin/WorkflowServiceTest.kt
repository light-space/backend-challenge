import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.light.challenge.logic.core.TypeHelper
import org.light.challenge.logic.core.WorkflowService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.TestInstance
import org.light.challenge.data.EmployeesTable
import org.light.challenge.data.RulesTable
import org.light.challenge.data.WorkflowTable
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkflowServiceTest {

    private val db: Database

    init {
        db = Database.connect("jdbc:sqlite:memory:test?mode=memory&cache=shared", "org.sqlite.JDBC")
        transaction(db) {
            SchemaUtils.create(EmployeesTable, RulesTable, WorkflowTable)
        }
    }

    @AfterAll
    fun tearDown() {
        transaction(db) {
            SchemaUtils.drop(EmployeesTable, RulesTable, WorkflowTable)
        }
        transaction(db){close()}
        File("./memory").delete()
    }

    @Test
    fun `test addRule`() {
        val rule = TypeHelper.Rule(
            amountRange = Pair(7000.0, 10000.0),
            department = TypeHelper.Department.MARKETING,
            requiresManagerApproval = true,
            employeeUsername = "lsimon",
            contactMethod = TypeHelper.ContactMethod.EMAIL
        )
        val expectedResult = 1
        transaction(db){
            val result = WorkflowService().addRule(rule)
            assertEquals(expectedResult, result)
        }
    }


}