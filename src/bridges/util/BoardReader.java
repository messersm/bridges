package bridges.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.List;

import bridges.game.Board;
import bridges.game.Island;
import bridges.game.Bridge;

/**
 * Reader for files holding bridge game data.
 * <br><br>
 * Example usage (without error handling):
 * <pre>{@code
 * String filename = "example.bgs";
 *
 * try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
 * 		Board board = new BoardReader(br).read();
 * }}
 * </pre>
 *
 * @author Maik Messerschmidt
 */
public class BoardReader {
    private Board board = null;
    private int expectedIslandCount;
    private StreamTokenizer tokenizer;

    /**
     * Create a new BoardReader.
     *
     * @param reader - the Reader, we use to get the board data.
     */
    public BoardReader(Reader reader) {
        /*
         * Create a custom StreamTokenizer, which (only) uses '#' comments.
         * DO NOTE: This StreamTokenizer still accepts floats as valid numbers
         * (this is handled by parseInteger()).
         */
        tokenizer = new StreamTokenizer(reader);
        tokenizer.commentChar('#');
        final char[] ORDINARY_CHARS = {'/', '*', '(', ')', '|', ','};
        for (char c : ORDINARY_CHARS)
            tokenizer.ordinaryChar(c);
    }

    /**
     * Read the next token as an String or char (or fail).
     *
     * @return The parsed String (or char).
     * @throws SyntaxException if the next token is no String.
     * @throws IOException     propagated from tokenizer.nextToken().
     */
    private String parseString() throws IllegalArgumentException, IOException {
        int ttype = tokenizer.nextToken();
        if (ttype == StreamTokenizer.TT_EOF) {
            return null;
        } else if (ttype == StreamTokenizer.TT_NUMBER) {
            throw new SyntaxException(
                    "Failed to parse string (got number " + tokenizer.nval + " instead).");
        } else if (ttype == StreamTokenizer.TT_WORD) {
            return tokenizer.sval;
        }
        // char
        else
            return Character.toString((char) tokenizer.ttype);
    }

    /**
     * Read the next token as an integer (or fail).
     *
     * @return The parsed integer.
     * @throws SyntaxException if the next token is no integer.
     * @throws IOException     propagated from tokenizer.nextToken().
     */
    private int parseInteger() throws SyntaxException, IOException {
        int ttype = tokenizer.nextToken();
        if (ttype == StreamTokenizer.TT_EOF) {
            throw new SyntaxException("Failed to parse integer (reached EOF)");
        } else if (ttype == StreamTokenizer.TT_WORD) {
            throw new SyntaxException(
                    "Failed to parse integer (got string \"" + tokenizer.sval + "\" instead).");
        }
        // Found character
        else if (ttype != StreamTokenizer.TT_NUMBER) {
            throw new SyntaxException(
                    "Failed to parse integer (got character '" + (char) ttype + "' instead.");
        }

        double value = tokenizer.nval;
        // Check, if this double is a real integer.
        if ((double) ((int) value) != value)
            throw new SyntaxException(
                    "Failed to parse integer (got double '" + value + "' instead.");

        return (int) value;
    }

    /**
     * Read the next token as an boolean (or fail).
     *
     * @return true: if the token is "true", false: if the token is "false".
     * @throws SyntaxException if the next token is neither "true" nor "false".
     * @throws IOException     propagated from StreamTokenizer.nextToken().
     */
    private boolean parseBoolean() throws SyntaxException, IOException {
        String token = parseString();
        if (token.equals("true"))
            return true;
        else if (token.equals("false"))
            return false;
        else
            throw new SyntaxException("Failed to parse boolean (got '" + token + "' instead).");
    }

    /**
     * Ensure the next string return by parseString() is equal to the given value.
     *
     * @param expected The expected String.
     * @throws SyntaxException if the next String is not equal to the given argument.
     * @throws IOException     propagated from StreamTokenizer.nextToken().
     */
    private void expectString(String expected) throws SyntaxException, IOException {
        String s = parseString();
        if (s == null || !s.equals(expected)) {
            throw new SyntaxException(
                    "Expected '" + expected + "' (got '" + s + "' instead).");
        }
    }

    /**
     * Read the data given by the StreamReader and return a new Board.
     *
     * @return A new Board.
     * @throws SyntaxException   if the data doesn't follow the given EBNF-Syntax.
     * @throws SemanticException if the data is not valid.
     * @throws IOException       propagated from StreamTokenizer.nextToken().
     */
    public Board read() throws SyntaxException, SemanticException, IOException {
        //
        if (board != null)
            return board;

        readFieldSection();
        readIslandSection();
        // Make sure, the number of islands is correct.
        if (expectedIslandCount != board.getIslandCount())
            throw new SemanticException(
                    "Expected " + expectedIslandCount + " islands, got " + board.getIslandCount() + ".");
        readBridgesSection();
        return board;
    }

    /**
     * Read the ISLANDS section of the given data and process it.
     *
     * @throws SyntaxException   if the data doesn't follow the given EBNF-Syntax.
     * @throws SemanticException if the data is not valid.
     * @throws IOException       propagated from StreamTokenizer.nextToken().
     */
    private void readIslandSection() throws SyntaxException, SemanticException, IOException {
        int x;
        int y;
        int bridgeCount;
        String token;

        expectString("ISLANDS");

        // Add new islands, until we reach EOF or the BRIDGES section.
        while (true) {
            token = parseString();
            // End of file
            if (token == null)
                return;

                // Already reached bridges section - push the token back up and return
            else if (token.equals("BRIDGES")) {
                tokenizer.pushBack();
                return;
            }

            // Read x, y and bridgeCount
            else if (token.equals("(")) {
                x = parseInteger();
                expectString(",");
                y = parseInteger();
                expectString("|");
                bridgeCount = parseInteger();
                expectString(")");

                // Create the new island or throw an exception.
                try {
                    Island island = new Island(x, y, bridgeCount);
                    board.addIsland(island);
                } catch (IllegalArgumentException e) {
                    throw new SemanticException(e.getMessage());
                }
            } else {
                throw new SyntaxException(
                        "Expected 'BRIDGES', '(' or end of file. Got '" + token + "' instead).");
            }
        }
    }

    /**
     * Read the BRIDGES section of the given data and process it.
     *
     * @throws SyntaxException   if the data doesn't follow the given EBNF-Syntax.
     * @throws SemanticException if the data is not valid.
     * @throws IOException       propagated from StreamTokenizer.nextToken().
     */
    private void readBridgesSection() throws SyntaxException, SemanticException, IOException {
        String token;

        // Get the next token and make sure, we've either reached EOF or the BRIDGES section.
        token = parseString();
        if (token == null)
            return;
        else if (!token.equals("BRIDGES"))
            throw new SyntaxException(
                    "Expected 'BRIDGES', or end of file. Got '" + token + "' instead).");

        List<Island> islands = board.getIslands();
        Island island1;
        Island island2;
        int idx1;
        int idx2;
        boolean isDouble;

        while (true) {
            token = parseString();
            // End of file - stop processing.
            if (token == null)
                return;
                // New bridge. Get island indices and isDouble value.
            else if (token.equals("(")) {
                idx1 = parseInteger();
                expectString(",");
                idx2 = parseInteger();
                expectString("|");
                isDouble = parseBoolean();
                expectString(")");

                // Add the new bridge to the board or throw an exception.
                try {
                    island1 = islands.get(idx1);
                    island2 = islands.get(idx2);
                    board.addBridge(new Bridge(island1, island2, isDouble));
                } catch (IllegalArgumentException e) {
                    throw new SemanticException(e.getMessage());
                } catch (IndexOutOfBoundsException e) {
                    throw new SemanticException("At least one invalid island index: " + idx1 + ", " + idx2 + ".");
                }
            } else
                throw new SyntaxException(
                        "Expected 'BRIDGES', '(' or end of file. Got '" + token + "' instead).");
        }
    }

    /**
     * Read the FIELD section of the given data and process it.
     *
     * @throws SyntaxException   if the data doesn't follow the given EBNF-Syntax.
     * @throws SemanticException if the data is not valid.
     * @throws IOException       propagated from StreamTokenizer.nextToken().
     */
    private void readFieldSection() throws SyntaxException, SemanticException, IOException {
        // FIELD section
        expectString("FIELD");
        int width = parseInteger();
        expectString("x");
        int height = parseInteger();
        expectString("|");
        int islandCount = parseInteger();

        // Create the new board or throw an exception.
        try {
            board = new Board(width, height);
        } catch (IllegalArgumentException e) {
            throw new SemanticException(e.getMessage());
        }

        // Save the expected island count for later use.
        expectedIslandCount = islandCount;
    }

    /**
     * Helper class for SyntaxError and SemanticError
     *
     * @author Maik Messerschmidt
     */
    private abstract class BoardException extends IllegalArgumentException {
        private String msg;
        private int lineno;

        /**
         * Create a new BoardException.
         *
         * @param msg - the details of the thrown exception.
         */
        private BoardException(String msg) {
            this.msg = msg;
            lineno = BoardReader.this.tokenizer.lineno();
        }

        /**
         * Get details about this Exception.
         *
         * @return the details of this exception (including class name and line number).
         */
        @Override
        public String getMessage() {
            String s = this.getClass().getName() + " at line " + tokenizer.lineno() + ": ";
            s += msg;
            return s;
        }

        /**
         * Return the line number, where this exception occurred.
         *
         * @return the line number
         */
        public int lineno() {
            return lineno;
        }
    }

    /**
     * Returned by BoardReader.read(),
     * if the given data doesn't follow the defined EBNF-Syntax.
     *
     * @author Maik Messerschmidt
     */
    public class SyntaxException extends BoardException {
        /**
         * Create a new SyntaxException.
         *
         * @param msg - the details of the thrown exception.
         */
        public SyntaxException(String msg) {
            super(msg);
        }
    }

    /**
     * Returned by BoardReader.read(),
     * if the given data is invalid
     * (e.g. invalid coordinates for islands, etc.).
     *
     * @author Maik Messerschmidt
     */
    public class SemanticException extends BoardException {
        /**
         * Create a new SemanticException.
         *
         * @param msg - the details of the thrown exception.
         */
        public SemanticException(String msg) {
            super(msg);
        }
    }
}
