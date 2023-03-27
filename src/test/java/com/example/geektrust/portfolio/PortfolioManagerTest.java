package com.example.geektrust.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.example.geektrust.portfolio.Fund.FundDTO;
import com.example.geektrust.portfolio.Fund.TransactionRecord;
import org.junit.jupiter.params.provider.ValueSource;

public class PortfolioManagerTest {
    private PortfolioManager portfolioManager;

    @BeforeEach
    public void setUp() {
        portfolioManager = new PortfolioManager(Arrays.asList(FundType.values()));
        assertTrue(portfolioManager.getSip().get(FundType.EQUITY).compareTo(BigDecimal.ZERO) == 0);
        assertFalse(portfolioManager.getFunds().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(FundType.class)
    public void fundsAtInitialState(FundType fundType) {
        Map<FundType, List<FundDTO>> fundsAtInit = portfolioManager.getFunds();
        FundDTO fund = fundsAtInit.get(fundType).get(0);
        assertNotNull(fund);
        assertTrue(BigDecimal.ZERO.compareTo(fund.initial) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(fund.current) == 0);
    }

    @ParameterizedTest
    @EnumSource(FundType.class)
    public void verify_allocate(FundType fundType) {
        BigDecimal amount = BigDecimal.valueOf(100.00);
        portfolioManager.allocate(fundType, amount);
        FundDTO fund = portfolioManager.getFunds().get(fundType).get(0);
        TransactionRecord allocationRecord = fund.transactionHistory.get(Year.now().atMonth(Month.JANUARY)).get(0);
        assertTrue(amount.compareTo(fund.initial) == 0);
        assertTrue(amount.compareTo(fund.current) == 0);
        assertNotNull(allocationRecord);
        assertEquals(TransactionEvent.ALLOCATE, allocationRecord.getEvent());
    }

    @ParameterizedTest
    @EnumSource(FundType.class)
    public void verify_skipAllocateWithNegativeAmount(FundType fundType) {
        BigDecimal amount = BigDecimal.valueOf(-100.00);
        portfolioManager.allocate(fundType, amount);
        Map<FundType, List<FundDTO>> funds = portfolioManager.getFunds();
        // verify no change
        assertTrue(BigDecimal.ZERO.compareTo(funds.get(fundType).get(0).initial) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(funds.get(fundType).get(0).current) == 0);
    }

    @DisplayName("Monthly Change tests")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class MonthlyChangeTest {
        @BeforeEach
        public void setUpManagerWithEquityFund() {
            portfolioManager = new PortfolioManager(Arrays.asList(FundType.EQUITY));
            portfolioManager.allocate(FundType.EQUITY, BigDecimal.valueOf(200));
            portfolioManager.setSip(
                    Map.ofEntries(new AbstractMap.SimpleEntry<>(FundType.EQUITY, BigDecimal.valueOf(500))));
            // allocate: 200
            // sip: 500
        }

        @Test
        public void verify_applyPositiveChange() {
            Map<FundType, BigDecimal> change = new HashMap<>();
            change.put(FundType.EQUITY, BigDecimal.valueOf(10.00));
            portfolioManager.applyMonthlyChange(Month.JANUARY, change);

            // assert change record
            YearMonth atJan = Year.now().atMonth(Month.JANUARY);
            YearMonth atFeb = Year.now().atMonth(Month.FEBRUARY);
            FundDTO fund = portfolioManager.getFunds().get(FundType.EQUITY).get(0);
            List<TransactionRecord> transactionHistory_Jan = fund.transactionHistory.get(atJan);
            TransactionRecord actualChangeRecord = transactionHistory_Jan.get(transactionHistory_Jan.size() - 1);
            assertTransactionRecord(actualChangeRecord, atJan, TransactionEvent.MONTHLY_CHANGE,
                    BigDecimal.valueOf(20.00), Optional.of(BigDecimal.valueOf(220)));

            // assert sip for next month
            List<TransactionRecord> transactionHistory_Feb = fund.transactionHistory
                    .get(Year.now().atMonth(Month.FEBRUARY));
            TransactionRecord lastTransaction = transactionHistory_Feb.get(transactionHistory_Feb.size() - 1);
            assertTransactionRecord(lastTransaction, atFeb, TransactionEvent.MONTHLY_SIP,
                    BigDecimal.valueOf(500), Optional.of(BigDecimal.valueOf(720)));

            assertTrue(BigDecimal.valueOf(720.0).compareTo(fund.current) == 0); // 200 + 20 + 500
            assertTrue(BigDecimal.valueOf(200).compareTo(fund.initial) == 0);
        }

        @Test
        public void verify_applyNegativeChange() {
            Map<FundType, BigDecimal> change = new HashMap<>();
            change.put(FundType.EQUITY, BigDecimal.valueOf(-20.00));
            portfolioManager.applyMonthlyChange(Month.JANUARY, change);

            // assert change record
            YearMonth atJan = Year.now().atMonth(Month.JANUARY);
            YearMonth atFeb = Year.now().atMonth(Month.FEBRUARY);
            FundDTO fund = portfolioManager.getFunds().get(FundType.EQUITY).get(0);
            List<TransactionRecord> transactionHistory_Jan = fund.transactionHistory.get(atJan);
            TransactionRecord actualChangeRecord = transactionHistory_Jan.get(transactionHistory_Jan.size() - 1);
            assertTransactionRecord(actualChangeRecord,
                    atJan,
                    TransactionEvent.MONTHLY_CHANGE,
                    BigDecimal.valueOf(-40.00),
                    Optional.of(BigDecimal.valueOf(160))); // 200 * -.2 = -40

            // assert sip for next month
            List<TransactionRecord> transactionHistory_Feb = fund.transactionHistory.get(atFeb);
            TransactionRecord lastTransaction = transactionHistory_Feb.get(transactionHistory_Feb.size() - 1);
            assertTransactionRecord(lastTransaction,
                    atFeb,
                    TransactionEvent.MONTHLY_SIP,
                    BigDecimal.valueOf(500),
                    Optional.empty());

            assertTrue(BigDecimal.valueOf(660.0).compareTo(fund.current) == 0); // 200 - 40 + 500
            assertTrue(BigDecimal.valueOf(200).compareTo(fund.initial) == 0);
        }

        @Test
        public void verify_applyChangeForSecondMonth() {
            Map<FundType, BigDecimal> change = new HashMap<>();
            change.put(FundType.EQUITY, BigDecimal.valueOf(5.5));
            portfolioManager.applyMonthlyChange(Month.JANUARY, change);
            portfolioManager.applyMonthlyChange(Month.FEBRUARY, change);

            // assert feb change record
            YearMonth atFeb = Year.now().atMonth(Month.FEBRUARY);
            YearMonth atMar = Year.now().atMonth(Month.MARCH);
            FundDTO fund = portfolioManager.getFunds().get(FundType.EQUITY).get(0);
            List<TransactionRecord> transactionHistory_Feb = fund.transactionHistory.get(atFeb);
            TransactionRecord actualChangeRecord = transactionHistory_Feb.get(transactionHistory_Feb.size() - 1);
            assertTransactionRecord(actualChangeRecord,
                    atFeb,
                    TransactionEvent.MONTHLY_CHANGE,
                    BigDecimal.valueOf(39.105), // (200 + (200 * 0.055) + 500) * 0.055
                    Optional.of(BigDecimal.valueOf(750.105))); // (200 + (200 * 0.055) + 500) + 39.105
            // assert sip for march
            List<TransactionRecord> transactionHistory_Mar = fund.transactionHistory
                    .get(atMar);
            TransactionRecord lastTransaction = transactionHistory_Mar.get(transactionHistory_Mar.size() - 1);
            assertTransactionRecord(lastTransaction,
                    atMar,
                    TransactionEvent.MONTHLY_SIP,
                    BigDecimal.valueOf(500),
                    Optional.empty());

            assertTrue(BigDecimal.valueOf(1250.105).compareTo(fund.current) == 0); // 750.105 + 500
            assertTrue(BigDecimal.valueOf(200).compareTo(fund.initial) == 0);
        }

        @Test
        public void verify_applyChangeForJuneWaitsRebalanceBeforeSip() {
            Fund equity = new Fund(FundType.EQUITY);
            Fund debt = new Fund(FundType.DEBT);
            Fund gold = new Fund(FundType.GOLD);
            Map<FundType, BigDecimal> sip = new HashMap<>(); // 500 each

            // weight 60%, 30%, 10%
            YearMonth atJan = YearMonth.now().withMonth(1);
            YearMonth atJune = YearMonth.now().withMonth(6);

            equity.initialise(BigDecimal.valueOf(600), atJan);
            debt.initialise(BigDecimal.valueOf(300), atJan);
            gold.initialise(BigDecimal.valueOf(100), atJan);
            assertTrue(BigDecimal.valueOf(600).compareTo(equity.getInitial()) == 0);
            assertTrue(BigDecimal.valueOf(300).compareTo(debt.getInitial()) == 0);
            assertTrue(BigDecimal.valueOf(100).compareTo(gold.getInitial()) == 0);

            // new portfolio with funds we can access directly here in this test case
            portfolioManager = new PortfolioManager(Arrays.asList(equity, debt, gold), null);
            Stream.of(FundType.values()).forEach(type -> {
                portfolioManager.allocate(type, BigDecimal.valueOf(20000));
                sip.put(type, BigDecimal.valueOf(500));
            });
            portfolioManager.setSip(sip);
            // Insert a change record for june
            // (this satisfies the validations).
            equity.doTransaction(atJune, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(5000));// 600 + 5000 = 5600
            debt.doTransaction(atJune, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(1000));// 300 + 1000 = 1300
            gold.doTransaction(atJune, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(6000));// 100 + 6000 = 6100
            // verify the last transaction for june (this also verify no SIP applied)
            assertTransactionRecord(equity.lastTransaction().get(),
                    atJune,
                    TransactionEvent.MONTHLY_CHANGE,
                    BigDecimal.valueOf(5000),
                    Optional.of(BigDecimal.valueOf(5600)));
            assertTransactionRecord(debt.lastTransaction().get(),
                    atJune,
                    TransactionEvent.MONTHLY_CHANGE,
                    BigDecimal.valueOf(1000),
                    Optional.of(BigDecimal.valueOf(1300)));
            assertTransactionRecord(gold.lastTransaction().get(),
                    atJune,
                    TransactionEvent.MONTHLY_CHANGE,
                    BigDecimal.valueOf(6000),
                    Optional.of(BigDecimal.valueOf(6100)));
            // verify the current balance equal to june market change applied
            assertTrue(BigDecimal.valueOf(5600).compareTo(equity.getCurrent()) == 0);
            assertTrue(BigDecimal.valueOf(1300).compareTo(debt.getCurrent()) == 0);
            assertTrue(BigDecimal.valueOf(6100).compareTo(gold.getCurrent()) == 0);
        }
    }

    @DisplayName("Rebalance tests")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class RebalanceTest {
        private Fund equity;
        private Fund debt;
        private Fund gold;
        private Map<FundType, BigDecimal> sip = new HashMap<>(); // 500 each

        @BeforeEach
        public void initialiseFunds() {
            equity = new Fund(FundType.EQUITY);
            debt = new Fund(FundType.DEBT);
            gold = new Fund(FundType.GOLD);

            // weight 60%, 30%, 10%
            YearMonth atJan = YearMonth.now().withMonth(1);
            equity.initialise(BigDecimal.valueOf(600), atJan);
            debt.initialise(BigDecimal.valueOf(300), atJan);
            gold.initialise(BigDecimal.valueOf(100), atJan);
            assertTrue(BigDecimal.valueOf(600).compareTo(equity.getInitial()) == 0);
            assertTrue(BigDecimal.valueOf(300).compareTo(debt.getInitial()) == 0);
            assertTrue(BigDecimal.valueOf(100).compareTo(gold.getInitial()) == 0);

            portfolioManager = new PortfolioManager(Arrays.asList(equity, debt, gold), null);
            Stream.of(FundType.values()).forEach(type -> {
                portfolioManager.allocate(type, BigDecimal.valueOf(20000));
                sip.put(type, BigDecimal.valueOf(500));
            });
            portfolioManager.setSip(sip);
        }

        @Test
        public void verify_skipRebalanceWhenNoSufficientPreviousRecords() {
            portfolioManager.rebalance();
            assertTrue(BigDecimal.valueOf(600).compareTo(equity.getCurrent()) == 0);
            assertTrue(BigDecimal.valueOf(300).compareTo(debt.getCurrent()) == 0);
            assertTrue(BigDecimal.valueOf(100).compareTo(gold.getCurrent()) == 0);
        }

        @Test
        @ValueSource
        public void verify_rebalanceInJune() {
            YearMonth atJune = YearMonth.now().withMonth(6);
            // Insert a change record for june
            // (this satisfies the validations).
            equity.doTransaction(atJune, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(5000));// 600 + 5000 = 5600
            debt.doTransaction(atJune, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(1000));// 300 + 1000 = 1300
            gold.doTransaction(atJune, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(6000));// 100 + 6000 = 6100
            // portfolio total = 5700 + 1200 + 6100 = 13000

            Optional<Map<FundType, BigDecimal>> postRebalance = portfolioManager.rebalance();
            assertTrue(postRebalance.isPresent());
            assertTrue(BigDecimal.valueOf(7800).compareTo(postRebalance.get().get(FundType.EQUITY)) == 0);
            assertTrue(BigDecimal.valueOf(3900).compareTo(postRebalance.get().get(FundType.DEBT)) == 0);
            assertTrue(BigDecimal.valueOf(1300).compareTo(postRebalance.get().get(FundType.GOLD)) == 0);

            // verify current value with sip applied
            assertTrue(BigDecimal.valueOf(8300).compareTo(equity.getCurrent()) == 0); // 7800 + 500
            assertTrue(BigDecimal.valueOf(4400).compareTo(debt.getCurrent()) == 0); // 3900 + 500
            assertTrue(BigDecimal.valueOf(1800).compareTo(gold.getCurrent()) == 0); // 1300 + 500
        }

        @Test
        @ValueSource
        public void verify_rebalanceInDecember() {
            YearMonth atDecember = YearMonth.now().withMonth(12);
            // Insert a change record for june
            // (this satisfies validations).
            equity.doTransaction(atDecember, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(20890));// 700 + 20890
                                                                                                         // = 21590
            debt.doTransaction(atDecember, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(13464));// 200 + 13464 =
                                                                                                       // 13664
            gold.doTransaction(atDecember, TransactionEvent.MONTHLY_CHANGE, BigDecimal.valueOf(4012));// 100 + 4012 =
                                                                                                      // 4112
            // portfolio total = 21590 + 13664 + 4112 = 39366

            Optional<Map<FundType, BigDecimal>> postRebalance = portfolioManager.rebalance();
            assertTrue(postRebalance.isPresent());
            assertTrue(BigDecimal.valueOf(23619.6).compareTo(postRebalance.get().get(FundType.EQUITY)) == 0);
            assertTrue(BigDecimal.valueOf(11809.8).compareTo(postRebalance.get().get(FundType.DEBT)) == 0);
            assertTrue(BigDecimal.valueOf(3936.6).compareTo(postRebalance.get().get(FundType.GOLD)) == 0);

            // verify current value with sip applied
            assertTrue(BigDecimal.valueOf(24119.6).compareTo(equity.getCurrent()) == 0); // 23619.6 + 500
            assertTrue(BigDecimal.valueOf(12309.8).compareTo(debt.getCurrent()) == 0); // 11809.8 + 500
            assertTrue(BigDecimal.valueOf(4436.6).compareTo(gold.getCurrent()) == 0); // 3936.6 + 500
        }
    }

    @DisplayName("Monthly Balance tests")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class MonthlyBalanceTest {
        private Map<FundType, BigDecimal> sip = new HashMap<>();

        @BeforeEach
        public void initialiseAllFundsAndSetSip() {
            Stream.of(FundType.values()).forEach(type -> {
                portfolioManager.allocate(type, BigDecimal.valueOf(20000));
                sip.put(type, BigDecimal.valueOf(500));
            });
            portfolioManager.setSip(sip);
            // allocation = 20000
            // sip = 500 each
        }

        @ParameterizedTest
        @EnumSource(FundType.class)
        public void verify_balanceJustAfterAllocation(FundType type) {
            Optional<Map<FundType, BigDecimal>> actual = portfolioManager.calculateBalance(Month.JANUARY);
            assertTrue(actual.isPresent());
            assertTrue(BigDecimal.valueOf(20000).compareTo(actual.get().get(type)) == 0);
        }

        @ParameterizedTest
        @EnumSource(FundType.class)
        public void verify_balanaceAfterFewTransactions(FundType type) {
            // same chance for all funds (10.0)
            Map<FundType, BigDecimal> change = new HashMap<>();
            Stream.of(FundType.values()).forEach(eachType -> change.put(eachType, BigDecimal.TEN));

            // add few transactions
            portfolioManager.applyMonthlyChange(Month.JANUARY, change); // 20000 + 20000*0.1 = 22000
            portfolioManager.applyMonthlyChange(Month.FEBRUARY, change); // 22000 + 500 + 22500*0.1 = 22500+2250 = 24750
            portfolioManager.applyMonthlyChange(Month.MARCH, change); // 24750 + 500 + 25250*0.1 = 25250 + 2525 = 27775

            Optional<Map<FundType, BigDecimal>> actual = portfolioManager.calculateBalance(Month.MARCH);
            assertTrue(actual.isPresent());
            assertTrue(BigDecimal.valueOf(27775).compareTo(actual.get().get(type)) == 0);
        }

        @Test
        public void verify_emptyIfCanNotCalculate() {
            portfolioManager = new PortfolioManager(Arrays.asList(FundType.values()));
            assertFalse(portfolioManager.getFunds().isEmpty());
            assertTrue(portfolioManager.calculateBalance(Month.JANUARY).isEmpty());
        }
    }

    private void assertTransactionRecord(TransactionRecord actual,
            YearMonth expectedMonth,
            TransactionEvent expectedEvent,
            BigDecimal expectedAmount,
            Optional<BigDecimal> expectedBalance) {
        assertEquals(expectedMonth, actual.getMonth());
        assertEquals(expectedEvent, actual.getEvent());
        assertTrue(expectedAmount.compareTo(actual.getAmount()) == 0);
        if (expectedBalance.isPresent())
            assertTrue(expectedBalance.get().compareTo(actual.getBalance()) == 0);
    }
}
