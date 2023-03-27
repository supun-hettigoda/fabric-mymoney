
package com.example.geektrust.command;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.example.geektrust.portfolio.FundType;
import com.example.geektrust.portfolio.PortfolioManager;

public class SipCommand implements ExecutablePortfolioCommand {
    private final Map<FundType, BigDecimal> sip;

    SipCommand(final Map<FundType, BigDecimal> sip) {
        this.sip = sip;
    }

    @Override
    public void execute(PortfolioManager portfolio, Optional<Consumer<String>> commandExecutionStateReporter) {
        portfolio.setSip(sip);
    }

    public Map<FundType, BigDecimal> getSip() {
        return Collections.unmodifiableMap(sip);
    }
}
