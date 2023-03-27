package com.example.geektrust.platform;

import java.util.Optional;
import java.util.function.Consumer;

import com.example.geektrust.command.CommandBinding;
import com.example.geektrust.command.ExecutablePortfolioCommand;
import com.example.geektrust.portfolio.PortfolioManager;

public class MyMoneyPlatform {
    private final PortfolioManager portfolio;
    private final InputCommandProvider commandProvider;
    private final Optional<Consumer<String>> executionOutputReporter;

    private MyMoneyPlatform(final PortfolioManager portfolio, InputCommandProvider commandProvider,
            Optional<Consumer<String>> executionOutputReporter) {
        this.portfolio = portfolio;
        this.commandProvider = commandProvider;
        this.executionOutputReporter = executionOutputReporter;
    }

    public static final MyMoneyPlatform of(final PortfolioManager portfolio, InputCommandProvider commandProvider,
            Optional<Consumer<String>> executionOutputReporter) {
        return new MyMoneyPlatform(portfolio, commandProvider, executionOutputReporter);
    }

    public void execute(String inputLine) {
        Optional<ExecutablePortfolioCommand> executable = CommandBinding.toExecutable(inputLine);
        if (executable.isPresent()) {
            executable.get().execute(portfolio, executionOutputReporter);
        }
    }

    /**
     * This method starts the platform and continuously accept input commands
     * provided by the given {@code InputCommandProvider} until the provider has no
     * command to send.
     */
    public void start() {
        // continuously keep listening for new commands
        Optional<String> nextCommand = this.commandProvider.nextCommand();
        while (nextCommand.isPresent()) {
            this.execute(nextCommand.get());
            nextCommand = this.commandProvider.nextCommand();
        }
    }

    public void shutDown() {
        portfolio.clear();
    }
}
