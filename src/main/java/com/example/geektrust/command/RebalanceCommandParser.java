package com.example.geektrust.command;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RebalanceCommandParser implements PortfolioCommandParser {
    public static Pattern PATTERN = Pattern.compile(
            "^" + CommandBinding.REBALANCE.name() + ZERO_OR_MORE_WHITESPACE + "$");

    @Override
    public Optional<ExecutablePortfolioCommand> parse(String input) {
        Matcher matcher = PATTERN.matcher(input.trim());
        if (matcher.find() && matcher.hitEnd()) {
            return Optional.of(new RebalanceCommand());
        }

        return Optional.empty();
    }
}
