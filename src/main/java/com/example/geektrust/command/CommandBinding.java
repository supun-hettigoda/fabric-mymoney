
package com.example.geektrust.command;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum CommandBinding {
    ALLOCATE(AllocateCommandParser::new),
    SIP(SipCommandParser::new),
    CHANGE(ChangeCommandParser::new),
    BALANCE(BalanceCommandParser::new),
    REBALANCE(RebalanceCommandParser::new);

    private final Supplier<PortfolioCommandParser> parserSupplier;

    private CommandBinding(Supplier<PortfolioCommandParser> parserSupplier) {
        this.parserSupplier = parserSupplier;
    }

    public static Optional<ExecutablePortfolioCommand> toExecutable(String input) {
        // @formatter:off
        return Stream.of(values())
                .map(CommandBinding::getParser)
                .map(parser -> parser.parse(input))
                .filter(Optional::isPresent)
                .findFirst().orElse(Optional.empty());
        // @formatter:on
    }

    private static PortfolioCommandParser getParser(CommandBinding binding) {
        return binding.parserSupplier.get();
    }
}
