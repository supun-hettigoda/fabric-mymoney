package com.example.geektrust.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.geektrust.portfolio.FundType;

public class ChangeCommandParserTest {
    private ChangeCommandParser parser;

    @BeforeEach
    public void setUp() {
        parser = new ChangeCommandParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CHANGE 4.00% 10.00% 2.00% JANUARY",
            "CHANGE -10% 40.00% 0.00% FEBRUARY",
            "CHANGE 12.5% 12.5% 12.50% MARCH",
            "CHANGE 12.548% 12.50% 12.50% DECEMBER"
    })
    public void verify_commandForValidChangeInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertTrue(command.isPresent());
        assertSame(ChangeCommand.class, command.get().getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "change 10% 10% 10% JANUARY",
            "CHANGE 10 20 10 jan",
            "CHANGE 0.30 20 10 JANUARY",
            "CHANGE 30 -20 10",
            "CHANGE 56.9% 45% 23% gjjd",
            "CHANGE 556.9% 45% 23% JANUARY"
    })
    public void verify_skipInvalidChangeInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertTrue(command.isEmpty());
    }

    @Test
    public void verify_parsedChangeCommandProperties() {
        Optional<ExecutablePortfolioCommand> command = parser.parse("CHANGE 4% 10.00% -2.00% APRIL");
        assertSame(ChangeCommand.class, command.get().getClass());

        Map<FundType, BigDecimal> change = ((ChangeCommand) command.get()).getChange();
        assertTrue(BigDecimal.valueOf(4).compareTo(change.get(FundType.EQUITY)) == 0);
        assertTrue(BigDecimal.valueOf(10.00).compareTo(change.get(FundType.DEBT)) == 0);
        assertTrue(BigDecimal.valueOf(-2.00).compareTo(change.get(FundType.GOLD)) == 0);
        assertEquals(Month.APRIL, ((ChangeCommand) command.get()).getMonth());
    }
}
