package bridges.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import bridges.game.Board;
import bridges.game.BoardState;
import bridges.game.Bridge;
import bridges.game.Direction;
import bridges.game.Island;

import bridges.util.BoardGenerator;
import bridges.util.BoardMerger;
import bridges.util.BoardReader;
import bridges.util.BoardReader.SemanticException;
import bridges.util.BoardReader.SyntaxException;
import bridges.util.BoardSolver;
import bridges.util.BoardWriter;


/**
 * An observable, thread save game model that provides the functionality of
 * - bridges.game and
 * - bridges.util
 *
 * @author Maik Messerschmidt
 */
public class GameModel extends Observable {
    private Board board = null;
    private Object lock = new Object();
    private int interval = 1000;

    public GameModel() {
    }

    /**
     * Restarts the current game.
     */
    public void restart() {
        synchronized (lock) {
            if (board != null) {
                board.reset();
                changeAndNotify();
            }
        }
    }

    /**
     * Executes the next solve step.
     *
     * @return true, if a new step could be executed, false, if not.
     */
    public boolean nextStep() {
        synchronized (lock) {
            if (board == null)
                return false;
            Bridge bridge = BoardSolver.nextBridge(board);
            if (bridge != null) {
                /*
                 * If this is a double bridge and a single is
                 * not yet present, add the single bridge first.
                 */
                if (bridge.isDouble()) {
                    Bridge singleBridge = new Bridge(bridge.getFirstIsland(), bridge.getSecondIsland(), false);
                    if (board.canAdd(singleBridge))
                        bridge = singleBridge;
                }

                board.addBridge(bridge);
                changeAndNotify(bridge);
                return true;
            } else
                return false;
        }
    }

    /**
     * Generates a new board.
     *
     * @param width       - The width of the new board.
     * @param height      - The height of the new board.
     * @param islandCount - The Island count of the new board.
     * @throws IllegalArgumentException if invalid parameters have been given.
     */
    public void generate(int width, int height, int islandCount) throws IllegalArgumentException {
        synchronized (lock) {
            board = BoardGenerator.generate(width, height, islandCount);
            changeAndNotify();
        }
    }

    /**
     * Load a new board from the given filename.
     *
     * @param filename This full path and filename of the file to load.
     * @throws IOException       If there was an I/O-Error while reading the given file.
     * @throws SemanticException If the file content had semantical errors.
     * @throws SyntaxException   If the file content had syntactical errors.
     */
    public void load(String filename) throws SyntaxException, SemanticException, IOException {
        synchronized (lock) {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                board = new BoardReader(br).read();
                changeAndNotify();
            }
        }
    }

    /**
     * Save the current board to the given filename.
     *
     * @param filename - the file to save the board to.
     * @throws IOException if the file could not be written.
     */
    public void save(String filename) throws IOException {
        synchronized (lock) {
            try (Writer writer = new FileWriter(filename)) {
                writer.write(BoardWriter.boardToString(board));
            }
        }
    }

    /**
     * Return all bridges of the board.
     *
     * @return A list of all bridges on the board.
     */
    public List<Bridge> getBridges() {
        synchronized (lock) {
            if (board == null)
                return new ArrayList<Bridge>();
            else
                return board.bridges();
        }
    }

    /**
     * Return all bridges, that include the given island.
     *
     * @param island - the island, all bridges must include.
     * @return A list of all bridges on the board, which include the given island.
     */
    public List<Bridge> getBridges(Island island) {
        synchronized (lock) {
            if (board == null)
                return new ArrayList<Bridge>();
            else
                return board.bridges(island);
        }
    }

    /**
     * Return all bridges, that include the given island and are build in the given direction
     *
     * @param island - the island, all bridges must include.
     * @param dir    - the direction, the bridges must be built in.
     * @return A list of all bridges on the board, which include the given island in the given direction.
     */
    public List<Bridge> getBridges(Island island, Direction dir) {
        synchronized (lock) {
            List<Bridge> bridges = new ArrayList<Bridge>();

            if (board == null)
                return bridges;
            else {
                Bridge bridge = board.searchBridge(island, dir);
                if (bridge != null)
                    bridges.add(bridge);
                return bridges;
            }
        }
    }

    /**
     * Check, if the given bridge can be added to the board.
     *
     * @param bridge - the bridge, that is to be added.
     * @return true, if the bridge can be added, false otherwise.
     */
    public boolean isValid(Bridge bridge) {
        synchronized (lock) {
            if (board == null)
                return false;
            else
                return board.canAdd(bridge);
        }
    }

    /**
     * @return A list of all islands of the board.
     */
    public List<Island> getIslands() {
        synchronized (lock) {
            if (board == null)
                return new ArrayList<Island>();
            else
                return board.getIslands();
        }
    }

    /**
     * @return The width of the board.
     */
    public int getWidth() {
        synchronized (lock) {
            if (board == null)
                return 0;
            else
                return board.getWidth();
        }
    }

    /**
     * @return The height of the board.
     */
    public int getHeight() {
        synchronized (lock) {
            if (board == null)
                return 0;
            else
                return board.getHeight();
        }
    }

    /**
     * Check, if a board is currently present.
     *
     * @return true, if a board is present, false otherwise.
     */
    public boolean hasBoard() {
        synchronized (lock) {
            return board != null;
        }
    }

    /**
     * Return the state of the current board.
     *
     * @return The state of the board.
     * @see bridges.game.BoardState
     */
    public BoardState getState() {
        synchronized (lock) {
            if (board == null)
                return BoardState.NOBOARD;
            else if (board.isComplete())
                return BoardState.SOLVED;
            else {
                // TODO: Move this code where it belongs.
                List<Island> islands = board.getIslands();
                for (Island island : islands) {
                    List<Bridge> bridges = board.bridges(island);
                    if (island.getRequiredBridges() < Bridge.count(bridges))
                        return BoardState.INCORRECT;
                }

                if (BoardSolver.hasStep(board))
                    return BoardState.UNSOLVED;
                else
                    return BoardState.UNSOLVABLE;
            }
        }
    }

    /**
     * Remove a single bridge from the board, which covers the given bridge.
     * This will make a double bridge into a single bridge and
     * completely remove a single bridge.
     * <br><br>
     * Note: The isDouble attribute of the bridge parameter is ignored.
     *
     * @param bridge - The bridge, which is to be removed.
     */
    public void removeOneBridge(Bridge bridge) {
        synchronized (lock) {
            if (board != null) {
                Bridge newBridge = board.removeOneBridge(bridge);
                changeAndNotify(newBridge);
            }
        }
    }

    /**
     * Add the given bridge to the board (or do nothing).
     *
     * @param bridge - the bridge to be added to the board.
     */
    public void addBridge(Bridge bridge) {
        synchronized (lock) {
            if (board != null) {
                board.addBridge(bridge);
                changeAndNotify(bridge);
            }
        }
    }

    /**
     * Helper method, that sets the changed value of this Observable and notifies all observers.
     */
    private void changeAndNotify() {
        setChanged();
        notifyObservers();
    }

    /**
     * Helper method, that sets the changed value of this Observable and notifies all observers.
     *
     * @param bridge - the newly changed bridge of the board.
     */
    private void changeAndNotify(Bridge bridge) {
        setChanged();
        notifyObservers(bridge);
    }

    /**
     * Returns the island at the given coordinates or null.
     *
     * @param x - the x coordinate of the island.
     * @param y - the y coordinate of the island.
     * @return The island at the given coordinates or null.
     */
    public Island getIslandAt(int x, int y) {
        synchronized (lock) {
            if (board != null)
                return board.getIslandAt(x, y);
            else
                return null;
        }
    }

    /**
     * Return a bridge, which starts at the given island an goes into the given direction (or null).
     * <p>
     * Note: This only creates the bridge. It is _not_ added to the board.
     *
     * @param island - The Island to start from.
     * @param dir    - The Direction to go into.
     * @return A new Bridge object or null.
     */
    public Bridge createBridge(Island island, Direction dir) {
        synchronized (lock) {
            if (board != null)
                return board.createBridge(island, dir);
            else
                return null;
        }
    }

    /**
     * Merge the puzzles given by the filenames left and right.
     *
     * @param leftFile  The puzzle, that will be present on the left of the new puzzle.
     * @param rightFile The puzzle, that will be present on the right of the new puzzle.
     */
    public void merge(String leftFile, String rightFile) throws SyntaxException, SemanticException, IOException {
        synchronized (lock) {
            Board leftBoard;
            Board rightBoard;

            try (BufferedReader br = new BufferedReader(new FileReader(leftFile))) {
                leftBoard = new BoardReader(br).read();
            }

            try (BufferedReader br = new BufferedReader(new FileReader(rightFile))) {
                rightBoard = new BoardReader(br).read();
            }

            // If the above code succeeds, this should not throw an Exception.
            board = BoardMerger.merge(leftBoard, rightBoard, true);
            changeAndNotify();
        }
    }
}
