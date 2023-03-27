package com.example.geektrust.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.geektrust.portfolio.FundType;

public class AllocateCommandParserTest {
    private AllocateCommandParser parser;

    @BeforeEach
    public void setUp() {
        parser = new AllocateCommandParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALLOCATE 10 10 10",
            "ALLOCATE 30   20   10",
            "ALLOCATE 5670 2345 2345",
            "ALLOCATE 0 2345 2345"
    })
    public void verify_commandForValidAllocateInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertSame(AllocateCommand.class, command.get().getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALLCATE 10 10 10",
            "allocate 10 20 10",
            "ALLOCATE 0.30 20 10",
            "ALLOCATE 30 -20 10",
            "ALLOCATE 5670.9 2345.0 2345"
    })
    public void verify_skipInvalidAllocateInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertTrue(command.isEmpty());
    }

    @Test
    public void verify_parsedAllocateCommandProperties() {
        Optional<ExecutablePortfolioCommand> command = parser.parse("ALLOCATE 5670 2345 100");
        assertSame(AllocateCommand.class, command.get().getClass());

        Map<FundType, BigDecimal> allocations = ((AllocateCommand) command.get()).getAllocations();
        assertTrue(BigDecimal.valueOf(5670).compareTo(allocations.get(FundType.EQUITY)) == 0);
        assertTrue(BigDecimal.valueOf(2345).compareTo(allocations.get(FundType.DEBT)) == 0);
        assertTrue(BigDecimal.valueOf(100).compareTo(allocations.get(FundType.GOLD)) == 0);
    }
}
