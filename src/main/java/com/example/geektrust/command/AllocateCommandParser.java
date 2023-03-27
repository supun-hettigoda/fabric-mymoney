
package com.example.geektrust.command;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.geektrust.portfolio.FundType;

public class AllocateCommandParser implements PortfolioCommandParser {
    // pattern to match ALLOCATE [num] [num] [num]
    public static Pattern PATTERN = Pattern.compile(
            "^" + CommandBinding.ALLOCATE.name() + ONE_OR_MORE_WHITESPACE + CAPTURE_POSITIVE_INT
                    + ONE_OR_MORE_WHITESPACE
                    + CAPTURE_POSITIVE_INT + ONE_OR_MORE_WHITESPACE + CAPTURE_POSITIVE_INT + ZERO_OR_MORE_WHITESPACE
                    + "$");

    @Override
    public Optional<ExecutablePortfolioCommand> parse(String input) {
        Matcher matcher = PATTERN.matcher(input.trim());
        if (matcher.find() && matcher.hitEnd()) {
            // pattern make sure we don't need special handling for NumberFormat issues.
            Map<FundType, BigDecimal> allocations = new HashMap<>();
            allocations.put(FundType.EQUITY, new BigDecimal(matcher.group(1)));
            allocations.put(FundType.DEBT, new BigDecimal(matcher.group(2)));
            allocations.put(FundType.GOLD, new BigDecimal(matcher.group(3)));
            return Optional.of(new AllocateCommand(allocations));
        }

        return Optional.empty();
    }
}
