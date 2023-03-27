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

public class AllocateCommandTest {
    private AllocateCommand command;

    private PortfolioManager portfolio;
    private Consumer<String> output;
    Map<FundType, BigDecimal> allocations;

    @SuppressWarnings("unchecked") // skiping type warning for mock instances.
    @BeforeEach
    public void setUp() {
        portfolio = Mockito.mock(PortfolioManager.class);
        output = Mockito.mock(Consumer.class);

        allocations = new HashMap<>();
        allocations.put(FundType.GOLD, BigDecimal.valueOf(2000));
        allocations.put(FundType.EQUITY, BigDecimal.valueOf(100));
        allocations.put(FundType.DEBT, BigDecimal.valueOf(20));

        command = new AllocateCommand(allocations);
    }

    @Test
    public void verify_executeForAllFundTypesInTheAllocationsMap() {
        command.execute(portfolio, Optional.of(output));
        Mockito.verify(portfolio).allocate(FundType.EQUITY, BigDecimal.valueOf(100));
        Mockito.verify(portfolio).allocate(FundType.DEBT, BigDecimal.valueOf(20));
        Mockito.verify(portfolio).allocate(FundType.GOLD, BigDecimal.valueOf(2000));
        Mockito.verify(output, never()).accept(any());
    }
}
