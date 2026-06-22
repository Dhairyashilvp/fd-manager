package com.fdtracker.core.domain.util

import com.fdtracker.core.domain.model.PayoutFrequency
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object InterestCalculator {

    private val mc = MathContext(20, RoundingMode.HALF_UP)

    /**
     * Compound Interest (Cumulative):
     * A = P × (1 + r/n)^(n×t)
     */
    fun calculateMaturityAmount(
        principal: BigDecimal,
        annualRate: BigDecimal,
        tenureDays: Int,
        compounding: PayoutFrequency
    ): BigDecimal {
        val n = BigDecimal(compounding.periodsPerYear())
        val r = annualRate.divide(BigDecimal(100), mc)
        val t = BigDecimal(tenureDays).divide(BigDecimal(365), mc)
        val nt = n.multiply(t, mc)
        val base = BigDecimal.ONE.add(r.divide(n, mc))
        val amount = principal.multiply(pow(base, nt), mc)
        return amount.setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Simple Interest (Non-Cumulative):
     * I = P × r × t
     */
    fun calculateSimpleInterest(
        principal: BigDecimal,
        annualRate: BigDecimal,
        tenureDays: Int
    ): BigDecimal {
        val r = annualRate.divide(BigDecimal(100), mc)
        val t = BigDecimal(tenureDays).divide(BigDecimal(365), mc)
        return principal.multiply(r, mc).multiply(t, mc).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Accrued Interest (as of today):
     * A_today = P × (1 + r/n)^(n × elapsed/365) - P
     */
    fun calculateAccruedInterest(
        principal: BigDecimal,
        annualRate: BigDecimal,
        valueDate: LocalDate,
        currentDate: LocalDate,
        compounding: PayoutFrequency
    ): BigDecimal {
        val elapsed = ChronoUnit.DAYS.between(valueDate, currentDate)
        if (elapsed <= 0) return BigDecimal.ZERO

        val n = BigDecimal(compounding.periodsPerYear())
        val r = annualRate.divide(BigDecimal(100), mc)
        val t = BigDecimal(elapsed).divide(BigDecimal(365), mc)
        val nt = n.multiply(t, mc)
        val base = BigDecimal.ONE.add(r.divide(n, mc))
        val accrued = principal.multiply(pow(base, nt), mc).subtract(principal)
        return accrued.setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Premature Withdrawal Payout:
     * effective_rate = applicable_rate - penalty_rate
     * payout = P × (1 + effective_rate/n)^(n × elapsed/365)
     */
    fun calculatePrematureWithdrawal(
        principal: BigDecimal,
        applicableRate: BigDecimal,
        penaltyRate: BigDecimal,
        elapsedDays: Int,
        compounding: PayoutFrequency
    ): BigDecimal {
        val effectiveRate = applicableRate.subtract(penaltyRate)
        if (effectiveRate <= BigDecimal.ZERO) return principal

        val n = BigDecimal(compounding.periodsPerYear())
        val r = effectiveRate.divide(BigDecimal(100), mc)
        val t = BigDecimal(elapsedDays).divide(BigDecimal(365), mc)
        val nt = n.multiply(t, mc)
        val base = BigDecimal.ONE.add(r.divide(n, mc))
        return principal.multiply(pow(base, nt), mc).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Calculate maturity date from start date and tenure days
     */
    fun calculateMaturityDate(valueDate: LocalDate, tenureDays: Int): LocalDate {
        return valueDate.plusDays(tenureDays.toLong())
    }

    /**
     * Weighted average yield = Σ(principal_i × rate_i) / Σ(principal_i)
     */
    fun calculateWeightedAverageYield(
        principals: List<BigDecimal>,
        rates: List<BigDecimal>
    ): BigDecimal {
        if (principals.isEmpty() || principals.size != rates.size) return BigDecimal.ZERO

        val totalPrincipal = principals.fold(BigDecimal.ZERO) { acc, p -> acc.add(p) }
        if (totalPrincipal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO

        val weightedSum = principals.zip(rates)
            .fold(BigDecimal.ZERO) { acc, (p, r) -> acc.add(p.multiply(r, mc)) }

        return weightedSum.divide(totalPrincipal, mc).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Prefer exact BigDecimal integer exponentiation when possible.
     * Falls back to double-based power only for true fractional exponents.
     */
    private fun pow(base: BigDecimal, exponent: BigDecimal): BigDecimal {
        val integerExponent = exponent.toInt()
        if (exponent.compareTo(BigDecimal(integerExponent)) == 0) {
            return base.pow(integerExponent, mc)
        }

        val result = Math.pow(base.toDouble(), exponent.toDouble())
        return BigDecimal(result, mc)
    }
}
