package com.example.geektrust.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.example.geektrust.portfolio.Fund.TransactionRecord;

@DisplayName("Fund operation test suit")
class FundTest {
    private Fund fund;
    private Fund initialisedFund;
    private FundType TYPE = FundType.DEBT;
    private BigDecimal allocationAmount = BigDecimal.valueOf(100.00);
    private YearMonth Jan = YearMonth.of(2020, Month.JANUARY);
    private YearMonth Feb = YearMonth.of(2020, Month.FEBRUARY);
    private YearMonth Mar = YearMonth.of(2020, Month.MARCH);

    @BeforeEach
    public void setUp() {
        fund = new Fund(TYPE);
        assertTrue(BigDecimal.ZERO.compareTo(fund.getCurrent()) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(fund.getInitial()) == 0);
        assertTrue(fund.getTransactionHistory().isEmpty());

        initialisedFund = new Fund(TYPE);
        initialisedFund.initialise(allocationAmount, Jan);
    }

    @DisplayName("Initialise tests")
    @Nested
    class FundAllocationTest {
        @Test
        public void verify_afterInitialisationState() {
            assertEquals(allocationAmount, initialisedFund.getInitial());
            assertEquals(allocationAmount, initialisedFund.getCurrent());
            assertTransactionRecord(initialisedFund.lastTransaction().get(), Jan, TransactionEvent.ALLOCATE,
                    allocationAmount);
        }

        @Test
        public void verify_skipReinitialise() {
            BigDecimal reallocationAmount = BigDecimal.valueOf(300.00);
            initialisedFund.initialise(reallocationAmount, Jan); // reinitialise effort
            assertEquals(allocationAmount, initialisedFund.getInitial());
            assertEquals(allocationAmount, initialisedFund.getCurrent());
            assertTransactionRecord(initialisedFund.lastTransaction().get(), Jan, TransactionEvent.ALLOCATE,
                    allocationAmount);
        }
    }

    @DisplayName("Performs a transaction event tests")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class TransactionTest {
        @ParameterizedTest
        @EnumSource(TransactionEvent.class)
        public void verify_restrictPerformingTransactionsUntilInitialised(TransactionEvent event) {
            BigDecimal transactionAmount = BigDecimal.valueOf(100.00);
            YearMonth Jan = YearMonth.of(2020, Month.JANUARY);
            fund.doTransaction(Jan, event, transactionAmount);
            // verify no change
            assertTrue(BigDecimal.ZERO.compareTo(fund.getCurrent()) == 0);
            assertTrue(BigDecimal.ZERO.compareTo(fund.getInitial()) == 0);
            assertTrue(fund.getTransactionHistory().isEmpty());
        }

        Stream<TransactionEvent> transactionEventsForSingleValidTransaction() {
            return Stream.of(TransactionEvent.MONTHLY_CHANGE, TransactionEvent.MONTHLY_SIP, TransactionEvent.REBALANCE);
        }

        @ParameterizedTest
        @MethodSource("transactionEventsForSingleValidTransaction")
        public void verify_singleValidTransaction(TransactionEvent event) {
            BigDecimal transactionAmount = BigDecimal.valueOf(300.00);
            initialisedFund.doTransaction(Jan, event, transactionAmount);
            assertTrue(initialisedFund.getCurrent().compareTo(BigDecimal.valueOf(400.00)) == 0);
            assertTransactionRecord(initialisedFund.lastTransaction().get(), Jan, event, transactionAmount);
        }

        @Test
        public void verify_AllocateTransactionNotSupported() {
            BigDecimal transactionAmount = BigDecimal.valueOf(200.00);
            initialisedFund.doTransaction(Jan, TransactionEvent.MONTHLY_SIP, transactionAmount);
            assertTransactionRecord(initialisedFund.lastTransaction().get(), Jan, TransactionEvent.MONTHLY_SIP,
                    transactionAmount);
            BigDecimal reallocationAmount = BigDecimal.valueOf(100.00);
            initialisedFund.doTransaction(Jan, TransactionEvent.ALLOCATE, reallocationAmount);
            // verify no effect to the state
            assertTransactionRecord(initialisedFund.lastTransaction().get(), Jan, TransactionEvent.MONTHLY_SIP,
                    transactionAmount);
            assertTrue(initialisedFund.getCurrent().compareTo(BigDecimal.valueOf(300.00)) == 0);
        }
    }

    @DisplayName("Find last transaction tests")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class LastTransactionTest {
        @BeforeEach
        public void insertTransactions() {
            initialisedFund.doTransaction(Jan, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(10));
            initialisedFund.doTransaction(Feb, TransactionEvent.MONTHLY_SIP, BigDecimal.valueOf(100));
            initialisedFund.doTransaction(Feb, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(-20));
            initialisedFund.doTransaction(Mar, TransactionEvent.MONTHLY_SIP, BigDecimal.valueOf(300));
        }

        @Test
        public void verify_lastTransaction() {
            Optional<TransactionRecord> lastTransaction = initialisedFund.lastTransaction();
            assertTrue(lastTransaction.isPresent());
            assertTransactionRecord(lastTransaction.get(), Mar, TransactionEvent.MONTHLY_SIP, BigDecimal.valueOf(300));
        }

        @Test
        public void verify_lastChangeTransaction() {
            Optional<TransactionRecord> lastChange = initialisedFund.lastTransactionOf(TransactionEvent.MONTHLY_CHANGE);
            assertTrue(lastChange.isPresent());
            assertTransactionRecord(lastChange.get(), Feb, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(-20));
        }

        @Test
        public void verify_lastTransactionWhenNotInitialised() {
            Optional<TransactionRecord> lastTransaction = fund.lastTransaction();
            assertFalse(lastTransaction.isPresent());
        }

        @Test
        public void verify_lastChangeTransactionWhenNotInitialised() {
            Optional<TransactionRecord> lastChange = fund.lastTransactionOf(TransactionEvent.MONTHLY_CHANGE);
            assertFalse(lastChange.isPresent());
        }

        @Test
        public void verify_lastChangeTransactionPresentsInLastEntry() {
            initialisedFund.doTransaction(Mar, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(400));
            Optional<TransactionRecord> lastChange = initialisedFund.lastTransactionOf(TransactionEvent.MONTHLY_CHANGE);
            assertTrue(lastChange.isPresent());
            assertTransactionRecord(lastChange.get(), Mar, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(400));
        }
    }

    @DisplayName("Balance tests")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class BalanceTest {
        @BeforeEach
        public void insertTransactions() {
            // allocation=100 (allocationAmount)
            initialisedFund.doTransaction(Jan, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(10));
            initialisedFund.doTransaction(Feb, TransactionEvent.MONTHLY_SIP, BigDecimal.valueOf(100));
            initialisedFund.doTransaction(Feb, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(-20));
            initialisedFund.doTransaction(Mar, TransactionEvent.MONTHLY_SIP, BigDecimal.valueOf(300));
        }

        @Test
        public void verify_balanceForImmediateLastMonth() {
            Optional<BigDecimal> actual = initialisedFund.balanceOf(Month.MARCH);
            assertTrue(actual.isPresent());
            assertTrue(BigDecimal.valueOf(490).compareTo(actual.get()) == 0); // 100 + 10 +100 -20 +300 = 490
        }

        @Test
        public void verify_balanceForFirstMonth() {
            Optional<BigDecimal> actual = initialisedFund.balanceOf(Month.JANUARY);
            assertTrue(actual.isPresent());
            assertTrue(BigDecimal.valueOf(110).compareTo(actual.get()) == 0); // 100 + 10 = 110
        }

        @Test
        public void verify_balanceWhenMonthDataIsNotPresents() {
            Optional<BigDecimal> actual = initialisedFund.balanceOf(Month.DECEMBER);
            assertTrue(actual.isEmpty());
        }

        @Test
        public void verify_emptyWhenBeforeInitialised() {
            assertTrue(fund.balanceOf(Month.JANUARY).isEmpty());
        }
    }

    private void assertTransactionRecord(TransactionRecord actual, YearMonth expectedMonth,
            TransactionEvent expectedEvent,
            BigDecimal expectedAmount) {
        assertEquals(expectedMonth, actual.getMonth());
        assertEquals(expectedEvent, actual.getEvent());
        assertEquals(expectedAmount, actual.getAmount());
    }
}