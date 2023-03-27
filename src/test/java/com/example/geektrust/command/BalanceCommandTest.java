package com.example.geektrust.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.geektrust.portfolio.FundType;
import com.example.geektrust.portfolio.PortfolioManager;

public class BalanceCommandTest {
    private BalanceCommand command;

    private PortfolioManager portfolio;
    private Consumer<String> output;
    Map<FundType, BigDecimal> balancedResult;

    @SuppressWarnings("unchecked") // skiping Consumer type warning as using a mock instance.
    @BeforeEach
    public void setUp() {
        portfolio = Mockito.mock(PortfolioManager.class);
        output = Mockito.mock(Consumer.class);

        balancedResult = new HashMap<>();
        command = new BalanceCommand(Month.MARCH);
    }

    @Test
    public void verify_validOutputOrder() {
        balancedResult.put(FundType.GOLD, BigDecimal.valueOf(2000));
        balancedResult.put(FundType.EQUITY, BigDecimal.valueOf(100));
        balancedResult.put(FundType.DEBT, BigDecimal.valueOf(20));

        when(portfolio.calculateBalance(Month.MARCH)).thenReturn(Optional.of(balancedResult));
        command.execute(portfolio, Optional.of(output));
        Mockito.verify(output).accept("100 20 2000");
    }

    @Test
    public void verify_noOutPutWhenBalanceCanNotCalculate() {
        when(portfolio.calculateBalance(Month.MARCH)).thenReturn(Optional.empty());
        command.execute(portfolio, Optional.of(output));
        Mockito.verify(output, never()).accept(any());
    }
}
