package com.example.geektrust.command;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class RebalanceCommandParserTest {
    private RebalanceCommandParser parser;

    @BeforeEach
    public void setUp() {
        parser = new RebalanceCommandParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "REBALANCE",
            "REBALANCE   "
    })
    public void verify_commandForValidRebaseInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertTrue(command.isPresent());
        assertSame(RebalanceCommand.class, command.get().getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "rebalance",
            "rbalance"
    })
    public void verify_skipInvalidRebalanceInputs(String input) {
        Optional<ExecutablePortfolioCommand> command = parser.parse(input);
        assertTrue(command.isEmpty());
    }
}
