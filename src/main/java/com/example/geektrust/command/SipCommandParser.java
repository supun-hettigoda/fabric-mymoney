package com.example.geektrust.command;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.geektrust.portfolio.FundType;

public class SipCommandParser implements PortfolioCommandParser {
    // pattern to match SIP [num] [num] [num]
    public static Pattern PATTERN = Pattern.compile(
            "^" + CommandBinding.SIP.name() + ONE_OR_MORE_WHITESPACE + CAPTURE_POSITIVE_INT + ONE_OR_MORE_WHITESPACE
                    + CAPTURE_POSITIVE_INT + ONE_OR_MORE_WHITESPACE + CAPTURE_POSITIVE_INT + ZERO_OR_MORE_WHITESPACE
                    + "$");

    @Override
    public Optional<ExecutablePortfolioCommand> parse(String input) {
        Matcher matcher = PATTERN.matcher(input.trim());
        if (matcher.find() && matcher.hitEnd()) {
            // pattern make sure we don't need special handling for NumberFormat issues.
            Map<FundType, BigDecimal> sip = new HashMap<>();
            sip.put(FundType.EQUITY, new BigDecimal((matcher.group(1))));
            sip.put(FundType.DEBT, new BigDecimal((matcher.group(2))));
            sip.put(FundType.GOLD, new BigDecimal(matcher.group(3)));
            return Optional.of(new SipCommand(sip));
        }

        return Optional.empty();
    }
}
