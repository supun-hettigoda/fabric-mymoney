package com.example.geektrust.portfolio;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

public class Fund {
    /**
     * READ only transaction record details.
     */
    public class TransactionRecord {
        private final YearMonth month;
        private final TransactionEvent event;
        private final BigDecimal amount;
        private final BigDecimal balance;

        public TransactionRecord(YearMonth month, TransactionEvent event, BigDecimal amount, BigDecimal balance) {
            this.month = month;
            this.event = event;
            this.amount = amount;
            this.balance = balance;
        }

        public YearMonth getMonth() {
            return month;
        }

        public TransactionEvent getEvent() {
            return event;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public BigDecimal getBalance() {
            return balance;
        }
    }

    private final FundType type;
    private YearMonth start;
    private BigDecimal current;
    private final NavigableMap<YearMonth, List<TransactionRecord>> trasactionHistory; // order by insert

    Fund(final FundType type) {
        this.type = type;
        this.current = BigDecimal.ZERO;
        this.trasactionHistory = new TreeMap<>();
    }

    public FundType getType() {
        return type;
    }

    public BigDecimal getInitial() {
        return !isInitialised() ? BigDecimal.ZERO
                : this.trasactionHistory.get(this.start).stream()
                        .filter(record -> TransactionEvent.ALLOCATE.equals(record.event)).findFirst().get().getAmount();
    }

    /**
     * @param fundStart has to be greater than zero to initialise a fund.
     */
    public void initialise(BigDecimal allocation, YearMonth fundStart) {
        if (isInitialised())
            return; // already initialised

        if (allocation.compareTo(BigDecimal.ZERO) < 0)
            return;

        this.start = fundStart;
        this.current = allocation;
        insertTransactionRecord(fundStart, TransactionEvent.ALLOCATE, allocation);
    }

    private boolean isInitialised() {
        return !(this.current.equals(BigDecimal.ZERO) && this.trasactionHistory.isEmpty());
    }

    public BigDecimal getCurrent() {
        return this.current;
    }

    /**
     * Fund has to be initialised to perform a transaction.
     * 
     * @param event  ALLOCATE event not supports as a transaction.
     * @param amount negative amount means a deduction from the current balance.
     */
    public void doTransaction(YearMonth month, TransactionEvent event, BigDecimal amount) {
        if (!isInitialised() || TransactionEvent.ALLOCATE.equals(event))
            return;

        this.current = this.current.add(amount);
        insertTransactionRecord(month, event, amount);
    }

    private void insertTransactionRecord(YearMonth month, TransactionEvent event, BigDecimal amount) {
        TransactionRecord record = new TransactionRecord(month, event, amount, this.current);
        List<TransactionRecord> existingEvents = this.trasactionHistory.putIfAbsent(month,
                new ArrayList<>(Arrays.asList(record)));
        if (existingEvents != null)
            existingEvents.add(record);
    }

    /**
     * @param month last month of.
     * @return The balance of the last transaction recorded in the last month
     *         matching the given month, empty if no such record exists.
     */
    public Optional<BigDecimal> balanceOf(Month month) {
        Iterator<YearMonth> descendingKeyIterator = this.trasactionHistory.navigableKeySet().descendingIterator();
        while (descendingKeyIterator.hasNext()) {
            // Last month matching the given month.
            YearMonth it = descendingKeyIterator.next();
            if (month.equals(it.getMonth())) {
                List<TransactionRecord> records = this.trasactionHistory.get(it);
                return Optional.of(records.get(records.size() - 1).balance);
            }
        }
        return Optional.empty();
    }

    /**
     * @return the last recorded transaction, empty if no transactions yet.
     */
    public Optional<TransactionRecord> lastTransaction() {
        if (!isInitialised())
            return Optional.empty();

        List<TransactionRecord> lastMonthHistory = this.trasactionHistory.lastEntry().getValue();
        return Optional.of(lastMonthHistory.get(lastMonthHistory.size() - 1));
    }

    public Optional<TransactionRecord> lastTransactionOf(TransactionEvent event) {
        Iterator<YearMonth> descendingKeyIterator = this.trasactionHistory.navigableKeySet().descendingIterator();
        while (descendingKeyIterator.hasNext()) {
            Optional<TransactionRecord> changeTransaction = this.trasactionHistory.get(descendingKeyIterator.next())
                    .stream()
                    .filter(record -> event.equals(record.event))
                    .findFirst();
            if (changeTransaction.isPresent())
                return changeTransaction;
        }
        return Optional.empty();
    }

    public Map<YearMonth, List<TransactionRecord>> getTransactionHistory() {
        return Collections.unmodifiableMap(this.trasactionHistory);
    }

    /**
     * @return ReadOnly DTO mapping of the Fund.
     */
    public class FundDTO {
        public final FundType type;
        public final BigDecimal initial;
        public final BigDecimal current;
        public final Map<YearMonth, List<TransactionRecord>> transactionHistory;

        public FundDTO(FundType type, BigDecimal initial, BigDecimal current,
                Map<YearMonth, List<TransactionRecord>> transactionHistory) {
            this.type = type;
            this.initial = initial;
            this.current = current;
            this.transactionHistory = transactionHistory;
        }

        public FundType getType() {
            return this.type;
        }
    }

    public FundDTO toDTO() {
        return new FundDTO(this.type, this.getInitial(), this.current, this.getTransactionHistory());
    }
}