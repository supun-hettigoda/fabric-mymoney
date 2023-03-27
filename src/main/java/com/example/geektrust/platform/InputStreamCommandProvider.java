
package com.example.geektrust.platform;

import java.io.InputStream;
import java.util.Optional;
import java.util.Scanner;

/**
 * Implementation of {@link InputCommandProvider} which reads the commands from
 * {@link InputStream}.
 */
public class InputStreamCommandProvider implements InputCommandProvider {
    private Scanner sc;

    public InputStreamCommandProvider(InputStream inputStream) {
        sc = new Scanner(inputStream);
    }

    @Override
    public Optional<String> nextCommand() {
        if (sc.hasNextLine())
            return Optional.of(sc.nextLine());

        return Optional.empty();
    }
}
