package com.example.geektrust.command;

import java.math.BigDecimal;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.geektrust.portfolio.FundType;

public class ChangeCommandParser implements PortfolioCommandParser {
    // pattern to match CHANGE [num] [num] [num] [MONTH]
    public static Pattern PATTERN = Pattern.compile(
            "^" + CommandBinding.CHANGE.name() + ONE_OR_MORE_WHITESPACE + CAPTURE_PERCENTAGE_VALUE
                    + ONE_OR_MORE_WHITESPACE + CAPTURE_PERCENTAGE_VALUE + ONE_OR_MORE_WHITESPACE
                    + CAPTURE_PERCENTAGE_VALUE + ONE_OR_MORE_WHITESPACE + CAPTURE_NEXT_WORD + ZERO_OR_MORE_WHITESPACE
                    + "$");

    @Override
    public Optional<ExecutablePortfolioCommand> parse(String input) {
        Matcher matcher = PATTERN.matcher(input.trim());
        if (matcher.find() && matcher.hitEnd()) {
            // pattern make sure we don't need special handling for NumberFormat issues.
            Map<FundType, BigDecimal> change = new HashMap<>();
            change.put(FundType.EQUITY, new BigDecimal(matcher.group(1)));
            change.put(FundType.DEBT, new BigDecimal(matcher.group(3)));
            change.put(FundType.GOLD, new BigDecimal(matcher.group(5)));

            try {
                Month month = Month.valueOf(matcher.group(7));
                return Optional.of(new ChangeCommand(change, month));
            } catch (Exception e) {
                // invalid text as month
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
