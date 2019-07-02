package bridges.util.tests;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import bridges.util.BoardReader;

public class BoardReaderTests {
    /**
     * Trying to read an empty file raises an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyFile() {
        try (Reader sr = new StringReader("")) {
            BoardReader reader = new BoardReader(sr);
            reader.read();
        } catch (IOException e) {
        }
    }
}
