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

public class SipCommandParserTest {
    private SipCommandParser parser;

    @BeforeEach
    public void setUp() {
        parser = new SipCommandParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SIP 10 10 10",
            "SIP 30   20   10",
            "SIP 5670 2345 2345",
            "SIP 0 2345 2345"
    })
    public void verify_commandForValidSipInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertSame(SipCommand.class, command.get().getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SiP 10 10 10",
            "sip 10 20 10",
            "SIP 0.30 20 10",
            "SIP 30 -20 10",
            "SIP 5670.9 2345.0 2345"
    })
    public void verify_skipInvalidSipInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertTrue(command.isEmpty());
    }

    @Test
    public void verify_parsedSipCommandProperties() {
        Optional<ExecutablePortfolioCommand> command = parser.parse("SIP 5670 2345 100");
        assertSame(SipCommand.class, command.get().getClass());

        Map<FundType, BigDecimal> allocations = ((SipCommand) command.get()).getSip();
        assertTrue(BigDecimal.valueOf(5670).compareTo(allocations.get(FundType.EQUITY)) == 0);
        assertTrue(BigDecimal.valueOf(2345).compareTo(allocations.get(FundType.DEBT)) == 0);
        assertTrue(BigDecimal.valueOf(100).compareTo(allocations.get(FundType.GOLD)) == 0);
    }
}
