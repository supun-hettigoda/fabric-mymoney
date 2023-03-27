package com.example.geektrust.command;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.example.geektrust.portfolio.FundType;
import com.example.geektrust.portfolio.PortfolioManager;

public class RebalanceCommand implements ExecutablePortfolioCommand {
    private static String INVALID = "CANNOT_REBALANCE";

    @Override
    public void execute(PortfolioManager portfolio, Optional<Consumer<String>> executionOutputReporter) {
        Optional<Map<FundType, BigDecimal>> postRebalance = portfolio.rebalance();
        String output;
        if (postRebalance.isPresent()) {
            BigDecimal equityBalance = postRebalance.get().get(FundType.EQUITY).setScale(0, RoundingMode.FLOOR);
            BigDecimal debtBalance = postRebalance.get().get(FundType.DEBT).setScale(0, RoundingMode.FLOOR);
            BigDecimal goldBalance = postRebalance.get().get(FundType.GOLD).setScale(0, RoundingMode.FLOOR);
            output = String.format("%s %s %s",
                    equityBalance.toString(),
                    debtBalance.toString(),
                    goldBalance.toString());
        }

        else
            output = INVALID;

        if (executionOutputReporter.isPresent())
            executionOutputReporter.get().accept(output);
    }
}
