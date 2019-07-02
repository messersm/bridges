package bridges.util.tests;

// 3rd party imports

import org.junit.Test;

import static org.junit.Assert.fail;

// Local imports
import bridges.game.Board;

import bridges.util.BoardGenerator;
import bridges.util.BoardSolver;
import bridges.util.BoardWriter;

public class BoardSolverTests {
    // Skip for now - solving still takes very long...
    // @Test
    public void testSolveRandom() {
        for (int i = 0; i < 100; i++) {
            Board board = BoardGenerator.generate();
            System.out.println("Running getAllSolutions() on:");
            System.out.println(BoardWriter.boardToString(board));
            BoardSolver.solve(board);

            if (!board.isComplete())
                fail("Couldn't solve board.\n" + BoardWriter.boardToString(board));
        }
    }
}
