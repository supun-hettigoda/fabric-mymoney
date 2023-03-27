
package com.example.geektrust.command;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.example.geektrust.portfolio.FundType;
import com.example.geektrust.portfolio.PortfolioManager;

public class ChangeCommand implements ExecutablePortfolioCommand {
    private final Map<FundType, BigDecimal> change;
    private final Month month;

    ChangeCommand(final Map<FundType, BigDecimal> change, Month month) {
        this.change = change;
        this.month = month;
    }

    @Override
    public void execute(PortfolioManager portfolio, Optional<Consumer<String>> commandExecutionStateReporter) {

        portfolio.applyMonthlyChange(month, change);
    }

    public Map<FundType, BigDecimal> getChange() {
        return Collections.unmodifiableMap(change);
    }

    public Month getMonth() {
        return month;
    }
}
