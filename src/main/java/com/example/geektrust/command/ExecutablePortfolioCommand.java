
package com.example.geektrust.command;

import java.util.Optional;
import java.util.function.Consumer;

import com.example.geektrust.portfolio.PortfolioManager;

public interface ExecutablePortfolioCommand {
    public void execute(PortfolioManager portfolio, Optional<Consumer<String>> executionOutputReporter);
}
