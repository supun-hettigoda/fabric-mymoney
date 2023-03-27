package com.example.geektrust.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Month;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BalanceCommandParserTest {
    private BalanceCommandParser parser;

    @BeforeEach
    public void setUp() {
        parser = new BalanceCommandParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "BALANCE  JANUARY",
            "BALANCE FEBRUARY ",
            "BALANCE MARCH",
            "BALANCE DECEMBER"
    })
    public void verify_commandForValidBalanceInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertTrue(command.isPresent());
        assertSame(BalanceCommand.class, command.get().getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "balance JANUARY",
            "BALANCE jan",
            "BALANCE  ",
            "BALANCE gjjd"
    })
    public void verify_skipInvalidBalanceInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertTrue(command.isEmpty());
    }

    @Test
    public void verify_parsedBalanceCommandProperties() {
        Optional<ExecutablePortfolioCommand> command = parser.parse("BALANCE APRIL");
        assertSame(BalanceCommand.class, command.get().getClass());
        assertEquals(Month.APRIL, ((BalanceCommand) command.get()).getMonth());
    }
}
