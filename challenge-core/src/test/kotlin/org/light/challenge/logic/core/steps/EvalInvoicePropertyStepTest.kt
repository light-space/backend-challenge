package org.light.challenge.logic.core.steps

import io.mockk.every
import io.mockk.mockk
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test
import org.light.challenge.data.models.CompanyId
import org.light.challenge.data.models.Department
import org.light.challenge.data.models.InvoiceDao
import org.light.challenge.data.models.InvoiceId
import org.light.challenge.data.models.InvoiceStatus
import java.util.UUID

internal class EvalInvoicePropertyStepTest {
    private val target = "target"
    private val wrongTarget = "wrongTarget"
    private val getSourceMockk = mockk<(InvoiceDao) -> String>()

    private val invoiceDao = InvoiceDao(
        id = InvoiceId(UUID.randomUUID()),
        companyId = CompanyId(UUID.randomUUID()),
        status = InvoiceStatus.PENDING,
        amount = Money.of(10, "EUR"),
        department = Department.FINANCE,
        requitesApproval = true
    )

    @Test
    fun `should answer true when property value is same as target`() {
        // Given
        every { getSourceMockk(invoiceDao) } returns target

        val evalStep = EvalInvoicePropertyStep(
            target = target,
            getSource = getSourceMockk
        )

        // When
        val result = evalStep(invoiceDao)

        // Then
        assert(result)
    }

    @Test
    fun `should answer false when property value is not same as target`() {
        // Given
        every { getSourceMockk(invoiceDao) } returns wrongTarget

        val evalStep = EvalInvoicePropertyStep(
            target = target,
            getSource = getSourceMockk
        )

        // When
        val result = evalStep(invoiceDao)

        // Then
        assert(!result)
    }
}