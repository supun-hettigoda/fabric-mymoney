package com.example.geektrust.portfolio;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import com.example.geektrust.portfolio.Fund.FundDTO;
import com.example.geektrust.portfolio.Fund.TransactionRecord;

public class PortfolioManager {
    private Map<FundType, BigDecimal> sip;
    private final List<Fund> funds;
    public static final List<Month> MONTHS_COMPULSORY_REBALANCE = Arrays.asList(Month.JUNE, Month.DECEMBER);

    public PortfolioManager(List<FundType> types) {
        this.funds = types.stream().map(fund -> new Fund(fund)).collect(Collectors.toList());

        // sip initialised with zero to no monthly contribution untill set
        this.sip = new HashMap<>();
        types.stream().forEach(type -> this.sip.put(type, BigDecimal.ZERO));
    }

    public PortfolioManager(List<Fund> funds, Map<FundType, BigDecimal> sip) {
        this.funds = funds;
        this.sip = sip;
    }

    /**
     * @param amount has to be greater than zero to initialise a fund.
     */
    public void allocate(FundType type, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            return; // skip initialising with negative allocation

        // Initialise the fund.
        this.fundOf(type).initialise(amount, Year.now().atMonth(Month.JANUARY));
    }

    /**
     * @param month        - last month of.
     * @param marketChange - market change by percentage. negative means a loss.
     */
    public void applyMonthlyChange(Month month, Map<FundType, BigDecimal> marketChange) {
        marketChange.forEach(
                (fundType, change) -> {
                    // validate
                    Fund fund = this.fundOf(fundType);
                    Optional<YearMonth> monthWithYear = validateAndGetChangeMonthWithYear(month, fund);

                    // proceed only the current month or the next month to the last transaction
                    if (monthWithYear.isPresent()) {
                        fund.doTransaction(
                                monthWithYear.get(),
                                TransactionEvent.MONTHLY_CHANGE,
                                fund.getCurrent().multiply(change).divide(BigDecimal.valueOf(100)));
                        openNextMonthWithSipIfNotPendingForRebalance(fund);
                    }
                });
    }

    private void openNextMonthWithSipIfNotPendingForRebalance(Fund fund) {
        if (!isPendingForRebalance(fund))
            // Apply next month sip immediately after this month's change.
            openNextMonthWithSip(fund);
    }

    private void openNextMonthWithSip(Fund fund) {
        // Apply next month sip immediately after this month's change.
        fund.doTransaction(
                fund.lastTransaction().get().getMonth().plusMonths(1),
                TransactionEvent.MONTHLY_SIP,
                this.sip.get(fund.getType()));
    }

    private boolean isPendingForRebalance(Fund fund) {
        // if last transaction recorded in the fund,
        // 1. is a MONTHLY_CHANGE
        // 2. belong to any month defined in MONTHS_COMPULSORY_REBALANCE
        TransactionRecord lastTransaction = fund.lastTransaction().get();
        Month currentMonth = lastTransaction.getMonth().getMonth();
        return (TransactionEvent.MONTHLY_CHANGE.equals(lastTransaction.getEvent())
                && MONTHS_COMPULSORY_REBALANCE.contains(currentMonth));
    }

    private Optional<YearMonth> validateAndGetChangeMonthWithYear(Month intendedMonth, Fund fund) {
        // last month change has to be recorded and processed to continue for the
        // intended month.
        Optional<TransactionRecord> lastChange = fund.lastTransactionOf(TransactionEvent.MONTHLY_CHANGE);
        if (lastChange.isPresent())
            if (intendedMonth.equals(lastChange.get().getMonth().getMonth().plus(1)))
                return Optional.of(lastChange.get().getMonth().plusMonths(1));
        // no change event yet recorded

        Optional<TransactionRecord> lastTransaction = fund.lastTransaction();
        if (lastTransaction.isPresent())
            if (intendedMonth.equals(lastTransaction.get().getMonth().getMonth()))
                return Optional.of(lastTransaction.get().getMonth());

        return Optional.empty();
    }

    /**
     * Calculate the portfolio balance of the given Month.
     *
     * @param of Last immediate month.
     * @return empty if balance can not be calculated for the given month.
     */
    public Optional<Map<FundType, BigDecimal>> calculateBalance(Month of) {
        Map<FundType, BigDecimal> balance = new HashMap<>();
        this.funds.forEach(fund -> {
            Optional<BigDecimal> balanceOfFund = fund.balanceOf(of);
            if (balanceOfFund.isPresent())
                balance.put(fund.getType(), fund.balanceOf(of).get());
        });

        if (balance.isEmpty())
            // empty if balance not valid
            return Optional.empty();

        return Optional.of(balance);
    }

    /**
     * Attempt a rebalance across the portfolio to have the same weight as per
     * initial allocation.
     * 
     * @return
     */
    public Optional<Map<FundType, BigDecimal>> rebalance() {
        Optional<YearMonth> rebalanceMonth = validateBeforeRebalance();
        if (rebalanceMonth.isPresent()) {
            BigDecimal initialTotal = this.funds
                    .stream()
                    .map(Fund::getInitial)
                    .reduce(BigDecimal.ZERO, (total, initial) -> total.add(initial));
            BigDecimal currentTotal = this.funds
                    .stream()
                    .map(Fund::getCurrent)
                    .reduce(BigDecimal.ZERO, (total, current) -> total.add(current));
            Map<FundType, BigDecimal> postRebalance = new HashMap<>();
            this.funds.forEach(fund -> {
                BigDecimal weight = fund.getInitial().divide(initialTotal);
                // calculate the re-balance value
                BigDecimal balancedValue = currentTotal
                        .multiply(weight)
                        .subtract(fund.getCurrent());
                // apply rebalance
                fund.doTransaction(rebalanceMonth.get(), TransactionEvent.REBALANCE, balancedValue);
                postRebalance.put(fund.getType(), fund.getCurrent());
            });
            this.funds.forEach(fund -> openNextMonthWithSipIfNotPendingForRebalance(fund));
            return Optional.of(postRebalance);
        }
        return Optional.empty();
    }

    private Optional<YearMonth> validateBeforeRebalance() {
        for (Fund fund : this.funds) {
            Optional<TransactionRecord> lastChange = fund.lastTransactionOf(TransactionEvent.MONTHLY_CHANGE);
            // rebalanced only allow in June and December
            if (lastChange.isPresent() && MONTHS_COMPULSORY_REBALANCE.contains(lastChange.get().getMonth().getMonth()))
                return Optional.of(lastChange.get().getMonth());
        }
        return Optional.empty();
    }

    private Fund fundOf(final FundType type) {
        return this.funds.stream().filter(fund -> type.equals(fund.getType())).findFirst().get();
    }

    public void setSip(Map<FundType, BigDecimal> sip) {
        this.sip = sip;
    }

    public Map<FundType, BigDecimal> getSip() {
        return Collections.unmodifiableMap(this.sip);
    }

    public Map<FundType, List<Fund.FundDTO>> getFunds() {
        return this.funds.stream().map(Fund::toDTO).collect(Collectors.groupingBy(FundDTO::getType));
    }

    public void clear() {
        this.funds.clear();
    }
}
