package bridges.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bridges.game.Board;
import bridges.game.Bridge;
import bridges.game.Island;

/**
 * Enables the user to write boards into files.
 * <br><br>
 * Example usage (without error handling):
 * <pre>{@code
 * Board board = new Board(10, 12);
 * // Add islands and bridges.
 * String filename = "example.bgs";
 *
 * try (Writer writer = new FileWriter(filename)) {
 * 		writer.write(BoardWriter.boardToString(board));
 * }}
 * </pre>
 *
 * @author Maik Messerschmidt
 */
public class BoardWriter {
    /**
     * Return a String representing the given board as defined by the given EBNF-syntax.
     *
     * @param board to be represented.
     * @return String representing the board.
     * @throws IllegalArgumentException if board is null.
     */
    public static String boardToString(Board board) throws IllegalArgumentException {
        if (board == null)
            throw new IllegalArgumentException("Board must not be null.");

        List<Island> islands = board.getIslands();
        List<Bridge> bridges = board.bridges();

        // Sort islands and bridges
        Collections.sort(islands);
        Collections.sort(bridges);

        // Create FIELD section
        String s = "FIELD\n";
        s += board.getWidth() + " x " + board.getHeight() + " | " + board.getIslandCount() + "\n\n";

        // Create ISLANDS section
        s += "ISLANDS\n";
        for (Island is : islands)
            s += "( " + is.getX() + ", " + is.getY() + " | " + is.getRequiredBridges() + " )\n";

        // Optionally create BRIDGES section
        if (!bridges.isEmpty()) {
            s += "\nBRIDGES\n";
            for (Bridge br : bridges) {
                /*
                 * DO NOTE: the Board class insures that there are only bridges present,
                 * which have corresponding islands already in the board. So the
                 * indexOf() calls below, will always succeed.
                 */
                int idx1 = islands.indexOf(br.getFirstIsland());
                int idx2 = islands.indexOf(br.getSecondIsland());
                if (idx1 > idx2) {
                    int tmp = idx1;
                    idx1 = idx2;
                    idx2 = tmp;
                }
                s += "( " + idx1 + ", " + idx2 + " | " + br.isDouble() + " )\n";
            }
        }
        return s;
    }
}
