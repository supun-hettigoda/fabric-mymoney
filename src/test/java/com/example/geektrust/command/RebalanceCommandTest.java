package com.example.geektrust.command;

import static org.mockito.Mockito.when;

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

public class RebalanceCommandTest {
    private RebalanceCommand command;

    private PortfolioManager portfolio;
    private Consumer<String> output;
    Map<FundType, BigDecimal> rebalancedResult;

    @SuppressWarnings("unchecked") // skiping Consumer type warning as using a mock instance.
    @BeforeEach
    public void setUp() {
        portfolio = Mockito.mock(PortfolioManager.class);
        output = Mockito.mock(Consumer.class);

        rebalancedResult = new HashMap<>();
        command = new RebalanceCommand();
    }

    @Test
    public void verify_validOutputOrder() {
        rebalancedResult.put(FundType.GOLD, BigDecimal.valueOf(2000));
        rebalancedResult.put(FundType.EQUITY, BigDecimal.valueOf(100));
        rebalancedResult.put(FundType.DEBT, BigDecimal.valueOf(20));

        when(portfolio.rebalance()).thenReturn(Optional.of(rebalancedResult));
        command.execute(portfolio, Optional.of(output));
        Mockito.verify(output).accept("100 20 2000");
    }

    @Test
    public void verify_cannotRebalanceOutput() {
        when(portfolio.rebalance()).thenReturn(Optional.empty());
        command.execute(portfolio, Optional.of(output));
        Mockito.verify(output).accept("CANNOT_REBALANCE");
    }
}
