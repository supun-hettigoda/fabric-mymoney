
package com.example.geektrust;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.geektrust.platform.InputCommandProvider;
import com.example.geektrust.platform.MyMoneyPlatform;
import com.example.geektrust.portfolio.FundType;
import com.example.geektrust.portfolio.PortfolioManager;

public class MyMoneyPlatformAcceptanceTest {
    private MyMoneyPlatform platform;
    private InputCommandProvider commandProvider;
    private Consumer<String> reporter;
    private PortfolioManager portfolio;

    @SuppressWarnings("unchecked") // skip mock types warnings.
    @BeforeEach
    public void setUp() {
        commandProvider = Mockito.mock(InputCommandProvider.class);
        reporter = Mockito.mock(Consumer.class);
        portfolio = new PortfolioManager(Arrays.asList(FundType.values()));
        platform = MyMoneyPlatform.of(portfolio, commandProvider, Optional.of(reporter));
    }

    @Test
    public void acceptance_minimumToRebalance() {
        // @formatter:off
        Mockito.when(commandProvider.nextCommand())
                .thenReturn(Optional.of("ALLOCATE 6000 3000 1000"))
                .thenReturn(Optional.of("SIP 2000 1000 500"))
                .thenReturn(Optional.of("CHANGE 4.00% 10.00% 2.00% JANUARY"))
                .thenReturn(Optional.of("CHANGE -10.00% 40.00% 0.00% FEBRUARY"))
                .thenReturn(Optional.of("CHANGE 12.50% 12.50% 12.50% MARCH"))
                .thenReturn(Optional.of("CHANGE 8.00% -3.00% 7.00% APRIL"))
                .thenReturn(Optional.of("CHANGE 13.00% 21.00% 10.50% MAY"))
                .thenReturn(Optional.of("CHANGE 10.00% 8.00% -5.00% JUNE"))
                .thenReturn(Optional.of("BALANCE MARCH"))
                .thenReturn(Optional.of("REBALANCE"))
                .thenReturn(Optional.empty());
        // @formatter:on

        platform.start();
        Mockito.verify(reporter).accept("10593 7897 2272");
        Mockito.verify(reporter).accept("23622 11811 3937");
    }

    @Test
    public void acceptance_noEnoughDataToRebalance() {
        // @formatter:off
        Mockito.when(commandProvider.nextCommand())
                .thenReturn(Optional.of("ALLOCATE 8000 6000 3500"))
                .thenReturn(Optional.of("SIP 3000 2000 1000"))
                .thenReturn(Optional.of("CHANGE 11.00% 9.00% 4.00% JANUARY"))
                .thenReturn(Optional.of("CHANGE -6.00% 21.00% -3.00% FEBRUARY"))
                .thenReturn(Optional.of("CHANGE 12.50% 18.00% 12.50% MARCH"))
                .thenReturn(Optional.of("CHANGE 23.00% -3.00% 7.00% APRIL"))
                .thenReturn(Optional.of("BALANCE MARCH"))
                .thenReturn(Optional.of("BALANCE APRIL"))
                .thenReturn(Optional.of("REBALANCE"))
                .thenReturn(Optional.empty());
        // @formatter:on

        platform.start();
        Mockito.verify(reporter).accept("15938 14553 6188");
        Mockito.verify(reporter).accept("23293 16056 7691");
        Mockito.verify(reporter).accept("CANNOT_REBALANCE");
    }

    @Test
    public void acceptance_skipInvalidCommands() {
        // @formatter:off
        Mockito.when(commandProvider.nextCommand())
                .thenReturn(Optional.of("ALLOCATE 8000 6000 3500"))
                .thenReturn(Optional.of("ANY INVALID WILL SAFELY SKIP"))
                .thenReturn(Optional.of("SIP 3000 2000 1000")) // this will be executed again
                .thenReturn(Optional.of("BALANCE JANUARY")) // 8000 6000 3500
                .thenReturn(Optional.of("CHANGE 11.00% 9.00% 4.00% JANUARY"))
                .thenReturn(Optional.of("BALANCE JANUARY")) // 8800 6540 3640
                .thenReturn(Optional.empty());
        // @formatter:on

        platform.start();
        Mockito.verify(reporter).accept("8000 6000 3500");
        Mockito.verify(reporter).accept("8880 6540 3640");
    }

    @Test
    public void acceptance_noOutPutIfCanNotCalculateBalanceForTheMonth() {
        // @formatter:off
        Mockito.when(commandProvider.nextCommand())
                .thenReturn(Optional.of("ALLOCATE 8000 6000 3500"))
                .thenReturn(Optional.of("SIP 3000 2000 1000"))
                .thenReturn(Optional.of("CHANGE 11.00% 9.00% 4.00% JANUARY"))
                .thenReturn(Optional.of("BALANCE JANUARY")) // 8800 6540 3640
                .thenReturn(Optional.of("BALANCE MARCH")) // no output
                .thenReturn(Optional.empty());
        // @formatter:on

        platform.start();
        Mockito.verify(reporter).accept("8880 6540 3640");
        Mockito.verify(reporter, times(1)).accept(any());
    }

    @Test
    public void acceptance_noSipAppliedMeansZeroSip() {
        // @formatter:off
        Mockito.when(commandProvider.nextCommand())
                .thenReturn(Optional.of("ALLOCATE 8000 6000 3500"))
                .thenReturn(Optional.of("CHANGE 11.00% 9.00% 4.00% JANUARY"))
                .thenReturn(Optional.of("BALANCE JANUARY")) // 8800 6540 3640
                .thenReturn(Optional.of("BALANCE FEBRUARY")) // same as January as zero sip
                .thenReturn(Optional.empty());
        // @formatter:on

        platform.start();
        Mockito.verify(reporter, times(2)).accept("8880 6540 3640");
    }
}
