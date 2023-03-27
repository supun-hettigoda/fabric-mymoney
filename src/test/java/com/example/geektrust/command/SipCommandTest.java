package com.example.geektrust.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.geektrust.portfolio.FundType;
import com.example.geektrust.portfolio.PortfolioManager;

public class SipCommandTest {
    private SipCommand command;

    private PortfolioManager portfolio;
    private Consumer<String> output;
    Map<FundType, BigDecimal> sip;

    @SuppressWarnings("unchecked") // skiping type warning for mock instances.
    @BeforeEach
    public void setUp() {
        portfolio = Mockito.mock(PortfolioManager.class);
        output = Mockito.mock(Consumer.class);
        sip = Mockito.mock(HashMap.class);
        command = new SipCommand(sip);
    }

    @Test
    public void verify_executeForAllFundTypesInTheAllocationsMap() {
        command.execute(portfolio, Optional.of(output));
        Mockito.verify(portfolio).setSip(sip);
        Mockito.verify(output, never()).accept(any());
    }
}
