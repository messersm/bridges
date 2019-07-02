package bridges.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import bridges.game.Board;
import bridges.game.Bridge;
import bridges.game.Island;
import bridges.game.Direction;

/**
 * Class that generates new bridges game boards.
 * <br><br>
 * <p>
 * Usage: BoardGenerator.generate(...).
 * <br><br>
 * <p>
 * Note: Since the board generation is completely
 * randomized, the BoardGenerator gives up after
 * DEFAULT_TRIES tries in order to avoid deadlocks.
 * <br><br>
 * To let the generator run forever and still
 * get completely random boards, use
 * {@code}<pre>
 * int width = BoardGenerator.randomWidth();
 * int height = BoardGenerator.randomHeight();
 * int islandCount = BoardGenerator.randomIslandCount(width, height);
 * Board board = BoardGenerator.generate(width, height, islandCount, -1);
 * </pre>
 *
 * @author Maik Messerschmidt
 */
public class BoardGenerator {
    /**
     * The minimal width allowed for a generated board.
     */
    final public static int MIN_WIDTH = 4;

    /**
     * The maximal width allowed for a generated board.
     */
    final public static int MAX_WIDTH = 25;

    /**
     * The minimal height allowed for a generated board.
     */
    final public static int MIN_HEIGHT = 4;

    /**
     * The maximal height allowed for a generated board.
     */
    final public static int MAX_HEIGHT = 25;
    /**
     * The minimal number of islands allowed for a generated
     * board. The maximal number of allowed islands depends
     * on the width and height of a board and can be
     * calculated using maxIslandCount().
     */
    final public static int MIN_ISLAND_COUNT = 2;

    /**
     * The default number of tries, used by generate().
     */
    final public static int DEFAULT_TRIES = 100;

    private static Random random = new Random();

    /**
     * Return a random allowed width for a new board.
     *
     * @return - The width as integer.
     */
    public static int randomWidth() {
        return MIN_WIDTH + random.nextInt(MAX_WIDTH - MIN_WIDTH);
    }

    /**
     * Return a random allowed height for a new board.
     *
     * @return - The height as integer.
     */
    public static int randomHeight() {
        return MIN_HEIGHT + random.nextInt(MAX_HEIGHT - MIN_HEIGHT);
    }

    /**
     * Return a random allowed island count for a new board.
     *
     * @param width  - The width of the new board.
     * @param height - The height of the new board.
     * @return - The island count as integer.
     */
    public static int randomIslandCount(int width, int height) {
        int max = maxIslandCount(width, height);
        if (max <= MIN_ISLAND_COUNT)
            return MIN_ISLAND_COUNT;
        else
            return MIN_ISLAND_COUNT + random.nextInt(max - MIN_ISLAND_COUNT);
    }

    /**
     * Return the maximal allowed island count for a new board.
     *
     * @param width  - The width of the new board.
     * @param height - The height of the new board.
     * @return - The maximal island count as integer.
     */
    public static int maxIslandCount(int width, int height) {
        return (int) (0.2 * width * height);
    }

    /**
     * Try to generate a new board with random width, height and island count.
     *
     * @return - The new Board.
     * @throws RuntimeException if the board couldn't be generated within the default number of tries
     *                          (see DEFAULT_TRIES).
     */
    public static Board generate() throws RuntimeException {
        // If this throws an IllegalArgumentException,
        // there is a programming error somewhere.
        return generate(randomWidth(), randomHeight());
    }

    /**
     * Try to generate a new board with the given width, height and a random island count.
     *
     * @param width  - The width of the new board.
     * @param height - The height of the new board.
     * @return - The new board.
     * @throws IllegalArgumentException if the width or height argument is too small or too big
     *                                  (check class constants for the exact values).
     * @throws RuntimeException         if the board couldn't be generated within the default number of tries
     *                                  (see DEFAULT_TRIES).
     */
    public static Board generate(int width, int height) throws IllegalArgumentException, RuntimeException {
        return generate(width, height, randomIslandCount(width, height));
    }

    /**
     * Try to generate a new board with the given width, height and island count.
     *
     * @param width       - The width of the new board.
     * @param height      - The height of the new board.
     * @param islandCount - The island count of the new board.
     * @return - The new board.
     * @throws IllegalArgumentException if the width, height or island count argument is too small or too big
     *                                  (check class constants and the maxIslandCount() method for the exact values).
     * @throws RuntimeException         if the board couldn't be generated within the default number of tries
     *                                  (see DEFAULT_TRIES).
     */
    public static Board generate(int width, int height, int islandCount) throws IllegalArgumentException, RuntimeException {
        return generate(width, height, islandCount, DEFAULT_TRIES);
    }

    /**
     * Try to generate a new board with the given width, height and island count within the given number of tries.
     *
     * @param width       - The width of the new board.
     * @param height      - The height of the new board.
     * @param islandCount - The island count of the new board.
     * @param tries       - The number of tries allowed to generate the board. Set to smaller than 0 to keep trying forever.
     * @return - The new board.
     * @throws IllegalArgumentException if the width, height or island count argument is too small or too big
     *                                  (check class constants and the maxIslandCount() method for the exact values).
     * @throws RuntimeException         if the board couldn't be generated within the given number of tries.
     */
    public static Board generate(int width, int height, int islandCount, int tries) throws IllegalArgumentException, RuntimeException {
        if (width < MIN_WIDTH || width > MAX_WIDTH)
            throw new IllegalArgumentException("Invalid width: " + width);

        if (height < MIN_HEIGHT || height > MAX_HEIGHT)
            throw new IllegalArgumentException("Invalid height: " + height);

        if (islandCount < MIN_ISLAND_COUNT || islandCount > maxIslandCount(width, height))
            throw new IllegalArgumentException("Invalid island count: " + islandCount);

        int t = 0;

        while (tries < 0 || t < tries) {
            // The Board constructor may also throw an IllegalArgumentException.
            Board board = new Board(width, height);
            boolean success = true;

            while (board.getIslandCount() < islandCount) {
                if (addIsland(board) == false) {
                    success = false;
                    break;
                }
            }

            // Building the board failed - try again.
            if (!success) {
                t += 1;
                continue;
            }

            /*
             * Because Island and Bridge are immutable objects
             * we can't simply add the islands and then set
             * the requiredBridges field to the right value.
             *
             * So we create a possible board in the first
             * step, which has incorrect required counts
             * and create a second, corrected board as
             * a second step.
             *
             * One could argue, that in that case Island
             * maybe shouldn't be immutable, but the
             * advantages overweight by far.
             */
            Board corrected = new Board(width, height);
            for (Island island : board.getIslands()) {
                int required = Bridge.count(board.bridges(island));
                corrected.addIsland(
                        new Island(island.getX(), island.getY(), required));
            }

            return corrected;
        }

        throw new RuntimeException("Couldn't generate Board( " + width + ", " + height + ") with + " + islandCount + " islands within " + tries + " tries.");
    }

    /**
     * Try to add a new island.
     *
     * @param board - The board to add an new island to.
     * @return true, if the island could be added, false otherwise.
     */
    private static boolean addIsland(Board board) {
        List<Island> islands = board.getIslands();
        if (islands.isEmpty()) {
            int x = random.nextInt(board.getWidth());
            int y = random.nextInt(board.getHeight());
            board.addIsland(new Island(x, y, 1));
            return true;
        }
        /*
         * Choose a random island from the board and
         * create a new neighbor in a random direction.
         * Since we don't know, if this will work for all
         * islands and directions, we simply iterate through
         * shuffled lists of both until we find a possibility.
         */
        else {
            Collections.shuffle(islands);
            for (Island island : islands) {
                List<Direction> directions = Arrays.asList(Direction.values());
                Collections.shuffle(directions);
                for (Direction dir : directions) {
                    if (addIsland(board, island, dir) == true)
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * Try to add a new island in the given direction.
     *
     * @param board  - The board to add an new island to.
     * @param origin - The Island used as point of origin.
     * @param dir    - The Direction where we go to from the point of origin.
     * @return true, if the island could be added, false otherwise.
     */
    private static boolean addIsland(Board board, Island origin, Direction dir) {
        List<Integer> xCoords = new ArrayList<Integer>();
        List<Integer> yCoords = new ArrayList<Integer>();

        // Get into a save distance from the current island.
        int x = origin.getX() + 2 * dir.dx;
        int y = origin.getY() + 2 * dir.dy;

        /*
         * Walk along the given direction, collecting
         * a list of possible coordinates, until
         * we encounter another island or the board borders.
         */
        while (true) {
            // Check borders.
            if (x < 0 || y < 0)
                break;
            else if (x >= board.getWidth() || y >= board.getHeight())
                break;

            // Check other islands.
            Island colliding = null;
            for (Island other : board.getIslands()) {
                if (other.collidesWith(new Island(x, y, 1))) {
                    colliding = other;
                    break;
                }

            }
            if (colliding != null)
                break;

            xCoords.add(x);
            yCoords.add(y);
            x += dir.dx;
            y += dir.dy;
        }

        // Randomly pick coordinates from the possible
        // ones - if any are possible at all. Then
        // add an island and a bridge to this island.
        if (xCoords.isEmpty())
            return false;

        // Note: Even though shuffling both
        // coordinate lists would normally result
        // in invalid coordinates, we make use of the
        // fact, that either x or y is always the
        // same, so shuffling them doesn't make a
        // difference.
        Collections.shuffle(xCoords);
        Collections.shuffle(yCoords);

        for (int i = 0; i < xCoords.size(); i++) {
            Island neighbor = new Island(xCoords.get(i), yCoords.get(i), 1);
            Bridge bridge = new Bridge(origin, neighbor, random.nextBoolean());

            // Add the island and the bridge, if the
            // bridge doesn't cross any bridge on the board
            // and the new island isn't cut by any bridges
            // already present on the board.
            if (bridge.crosses(board.bridges()))
                continue;

            if (neighbor.isCutBy(board.bridges()))
                continue;

            board.addIsland(neighbor);
            board.addBridge(bridge);
            return true;
        }

        return false;
    }
}
