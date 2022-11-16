package org.light.challenge.logic.core.steps

import org.javamoney.moneta.Money
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.light.challenge.data.models.CompanyId
import org.light.challenge.data.models.Department
import org.light.challenge.data.models.InvoiceDao
import org.light.challenge.data.models.InvoiceId
import org.light.challenge.data.models.InvoiceStatus
import java.util.UUID

internal class EvalInvoiceAmountStepTest {
    private val currencyCode = "EUR"
    private val tenEuros = Money.of(10, currencyCode)
    private val elevenEuros = Money.of(11, currencyCode)

    private val invoiceDao = InvoiceDao(
        id = InvoiceId(UUID.randomUUID()),
        companyId = CompanyId(UUID.randomUUID()),
        status = InvoiceStatus.PENDING,
        amount = tenEuros,
        department = Department.FINANCE,
        requitesApproval = true
    )

    @Nested
    inner class LowerInclusiveBoundTest {

        @Test
        fun `should answer true when bound is 10 and invoice amount is 10`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep(
                lowerAmountBound = tenEuros,
                isLowerBoundInclusive = true
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(result)
        }

        @Test
        fun `should answer false when bound is 11 and invoice amount is 10`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep(
                lowerAmountBound = elevenEuros,
                isLowerBoundInclusive = true
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(!result)
        }
    }

    @Nested
    inner class LowerNonInclusiveBoundTest {

        @Test
        fun `should answer true when bound is 10 and invoice amount is 11`() {
            // Given
            val invoice = invoiceDao.copy(amount = elevenEuros)
            val evalStep = EvalInvoiceAmountStep(
                lowerAmountBound = tenEuros,
                isLowerBoundInclusive = false
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(result)
        }

        @Test
        fun `should answer false when bound is 10 and invoice amount is 10`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep(
                lowerAmountBound = tenEuros,
                isLowerBoundInclusive = false
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(!result)
        }

        @Test
        fun `should answer false when bound is 10 and invoice amount is 10 - null default to false`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep(
                lowerAmountBound = tenEuros,
                isLowerBoundInclusive = null
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(!result)
        }
    }

    @Nested
    inner class UpperInclusiveBoundTest {

        @Test
        fun `should answer true when bound is 10 and invoice amount is 10`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep(
                upperAmountBound = tenEuros,
                isUpperBoundInclusive = true
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(result)
        }

        @Test
        fun `should answer false when bound is 10 and invoice amount is 11`() {
            // Given
            val invoice = invoiceDao.copy(amount = elevenEuros)
            val evalStep = EvalInvoiceAmountStep(
                upperAmountBound = tenEuros,
                isUpperBoundInclusive = true
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(!result)
        }
    }

    @Nested
    inner class UpperNonInclusiveBoundTest {

        @Test
        fun `should answer true when bound is 11 and invoice amount is 10`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep(
                upperAmountBound = elevenEuros,
                isUpperBoundInclusive = false
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(result)
        }

        @Test
        fun `should answer false when bound is 10 and invoice amount is 10`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep(
                upperAmountBound = tenEuros,
                isUpperBoundInclusive = false
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(!result)
        }

        @Test
        fun `should answer true when bound is 11 and invoice amount is 10 - null default to false`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep(
                upperAmountBound = elevenEuros,
                isUpperBoundInclusive = null
            )

            // When
            val result = evalStep(invoice)

            // Then
            assert(result)
        }
    }

    @Nested
    inner class EdgeCases {

        @Test
        fun `should answer true when there is no limit`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep()

            // When
            val result = evalStep(invoice)

            // Then
            assert(result)
        }

        @Test
        fun `should answer false when there is no limit`() {
            // Given
            val invoice = invoiceDao.copy(amount = tenEuros)
            val evalStep = EvalInvoiceAmountStep()

            // When
            val result = evalStep(invoice)

            // Then
            assert(result)
        }
    }
}