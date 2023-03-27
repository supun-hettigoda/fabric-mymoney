
package com.example.geektrust.command;

import java.util.Optional;

public interface PortfolioCommandParser {
    public static String CAPTURE_POSITIVE_INT = "(\\d+)";
    public static String CAPTURE_PERCENTAGE_VALUE = "(-?\\d{1,2}(\\.\\d+)?)%";
    public static String CAPTURE_NEXT_WORD = "([A-Z]+)";
    public static String ONE_OR_MORE_WHITESPACE = "\\s+";
    public static String ZERO_OR_MORE_WHITESPACE = "\\s*";

    public Optional<ExecutablePortfolioCommand> parse(String inputCommand);
}
