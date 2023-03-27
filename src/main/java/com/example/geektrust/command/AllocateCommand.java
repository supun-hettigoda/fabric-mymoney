
package com.example.geektrust.command;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.example.geektrust.portfolio.FundType;
import com.example.geektrust.portfolio.PortfolioManager;

public class AllocateCommand implements ExecutablePortfolioCommand {
    private final Map<FundType, BigDecimal> allocations;

    AllocateCommand(final Map<FundType, BigDecimal> allocations) {
        this.allocations = allocations;
    }

    @Override
    public void execute(PortfolioManager portfolio, Optional<Consumer<String>> commandExecutionStateReporter) {
        this.allocations.forEach((fund, amount) -> portfolio.allocate(fund, amount));
    }

    public Map<FundType, BigDecimal> getAllocations() {
        return Collections.unmodifiableMap(allocations);
    }
}
