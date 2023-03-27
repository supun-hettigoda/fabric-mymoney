package com.example.geektrust.command;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.example.geektrust.portfolio.FundType;
import com.example.geektrust.portfolio.PortfolioManager;

public class BalanceCommand implements ExecutablePortfolioCommand {
    private final Month month;

    BalanceCommand(Month month) {
        this.month = month;
    }

    @Override
    public void execute(PortfolioManager portfolio, Optional<Consumer<String>> executionOutputReporter) {
        Optional<Map<FundType, BigDecimal>> balanceResult = portfolio.calculateBalance(this.month);
        String output;
        if (balanceResult.isPresent()) {
            BigDecimal equityBalance = balanceResult.get().get(FundType.EQUITY).setScale(0, RoundingMode.FLOOR);
            BigDecimal debtBalance = balanceResult.get().get(FundType.DEBT).setScale(0, RoundingMode.FLOOR);
            BigDecimal goldBalance = balanceResult.get().get(FundType.GOLD).setScale(0, RoundingMode.FLOOR);
            output = String.format("%s %s %s",
                    equityBalance.toString(),
                    debtBalance.toString(),
                    goldBalance.toString());
            if (executionOutputReporter.isPresent())
                executionOutputReporter.get().accept(output);
        }
    }

    public Month getMonth() {
        return month;
    }
}
