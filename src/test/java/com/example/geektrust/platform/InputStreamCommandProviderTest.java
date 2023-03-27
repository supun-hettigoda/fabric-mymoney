
package com.example.geektrust.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class InputStreamCommandProviderTest {
    @Test
    public void test_getNextCommand() {
        InputStream is = new ByteArrayInputStream("ALLOCATE 10 10 10\r\nBALANCE JAN".getBytes()); // 2 lines
        InputStreamCommandProvider provider = new InputStreamCommandProvider(is);
        assertEquals("ALLOCATE 10 10 10", provider.nextCommand().get());
        assertEquals("BALANCE JAN", provider.nextCommand().get());

        // next attempt should result in an empty optional as it has only two lines.
        assertFalse(provider.nextCommand().isPresent());
    }
}
