package com.example.geektrust.command;

import java.time.Month;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BalanceCommandParser implements PortfolioCommandParser {
    // pattern to match BALANCE [MONTH]
    public static Pattern PATTERN = Pattern.compile(
            "^" + CommandBinding.BALANCE.name() + ONE_OR_MORE_WHITESPACE + CAPTURE_NEXT_WORD + ZERO_OR_MORE_WHITESPACE
                    + "$");

    @Override
    public Optional<ExecutablePortfolioCommand> parse(String input) {
        Matcher matcher = PATTERN.matcher(input.trim());
        if (matcher.find() && matcher.hitEnd()) {
            try {
                Month month = Month.valueOf(matcher.group(1));
                return Optional.of(new BalanceCommand(month));
            } catch (Exception e) {
                // invalid text as month
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
