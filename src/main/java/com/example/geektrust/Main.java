package com.example.geektrust;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import com.example.geektrust.platform.InputStreamCommandProvider;
import com.example.geektrust.platform.MyMoneyPlatform;
import com.example.geektrust.portfolio.FundType;
import com.example.geektrust.portfolio.PortfolioManager;

public class Main {
    public static void main(String[] args) {
        // Sample code to read from file passed as command line argument
        try {
            // the file to be opened for reading
            FileInputStream fis = new FileInputStream(args[0]);
            MyMoneyPlatform platform = MyMoneyPlatform.of(
                    new PortfolioManager(Arrays.asList(FundType.values())),
                    new InputStreamCommandProvider(fis),
                    Optional.of(System.out::println));
            platform.start();
            platform.shutDown();
        } catch (IOException e) {
        }
    }
}