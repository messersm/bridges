package bridges.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bridges.game.Board;
import bridges.game.Island;
import bridges.game.Bridge;

/**
 * Interface for algorithms which can be used by the BoardSolver class.
 *
 * @author Maik Messerschmidt
 */
interface Algorithm {
    /**
     * Return a possible bridge for the given board or null.
     *
     * @param board
     * @return A possible bridge.
     */
    public Bridge nextBridge(Board board);
}

/**
 * A solve algorithm, which simply makes use
 * of the required count of an island.
 *
 * @author Maik Messerschmidt
 */
class Required implements Algorithm {
    /**
     * Return a possible bridge for the given board or null.
     *
     * @param board
     * @return A possible bridge.
     */
    public Bridge nextBridge(Board board) {
        for (Island island : board.getIslands()) {
            int required = island.getRequiredBridges();
            List<Bridge> existing = board.bridges(island);

            /*
             * Count existing bridges and skip this island,
             * if it already has enough. The island may have
             * to many bridges already, if the user made a
             * mistake.
             */
            if (required <= Bridge.count(existing))
                continue;

            /*
             * This follows the strategies, described in
             * section 2.1. and 2.2.2 of the task:
             *
             * 1. Collect all valid neighbors, where we can
             *    actually build bridges to (that is: these
             *    bridges would not cross any existing
             *    bridges).
             * 2. Count the number of available bridges for
             *    each valid neighbor and add it to the
             *    single_neighbors or double_neighbors list
             *    depending on the numbers of bridges we can
             *    build to them.
             * 2. The available number of bridges that can be
             *    build is the sum over 2 for each
             *    double_neighbor and 1 for each single_neighbor.
             * 3. If it is equal to the number of required bridges
             *    we can build a bridge to any of these.
             *    Otherwise we know, that if the
             *    number of available bridges - 1 is
             *    the number of required bridges we can
             *    at least add a single bridge BUT ONLY,
             *    if all neighbors, which we still can
             *    build bridges to, are double neighbors.
             *
             * This algorithm returns the same results as the
             * 2*n and 2*n-1 method, but also includes cases,
             * like this one, where these methods fail:
             *
             *        2
             *
             *        4  1
             *
             *        1
             *
             * 4 < 2 * 3 and 4 < 2 * 3 - 1.
             */
            List<Island> singleNeighbors = new ArrayList<Island>();
            List<Island> doubleNeighbors = new ArrayList<Island>();

            for (Island neighbor : board.validNeighbors(island)) {
                // Build a list of bridges of this neighbor, which
                // excludes possible bridges to the current island
                List<Bridge> neighborBridges = board.bridges(neighbor);
                neighborBridges.remove(new Bridge(island, neighbor, false));
                neighborBridges.remove(new Bridge(island, neighbor, true));

                int count = Bridge.count(neighborBridges);
                int remainingForNeighbor = neighbor.getRequiredBridges() - count;

                if (remainingForNeighbor >= 2)
                    doubleNeighbors.add(neighbor);
                else if (remainingForNeighbor == 1)
                    singleNeighbors.add(neighbor);
            }

            int available = 2 * doubleNeighbors.size() + singleNeighbors.size();

            // All neighbors need the maximum number of bridges.
            if (required == available) {
                for (Island neighbor : doubleNeighbors) {
                    // Only add double bridge, if no double bridge exists.
                    Bridge bridge = new Bridge(island, neighbor, true);
                    if (!existing.contains(bridge))
                        return bridge;
                }
                for (Island neighbor : singleNeighbors) {
                    // Only add a bridge, if neither double nor single exists.
                    Bridge doubleBridge = new Bridge(island, neighbor, true);
                    Bridge bridge = new Bridge(island, neighbor, false);
                    if (!existing.contains(bridge) && !existing.contains(doubleBridge))
                        return bridge;
                }
            } else if (required == available - 1) {
                List<Island> existingNeighbors = new ArrayList<Island>();
                for (Bridge bridge : existing) {
                    // This add the current island to existingNeighbors,
                    // but this doesn't matter.
                    existingNeighbors.add(bridge.getFirstIsland());
                    existingNeighbors.add(bridge.getSecondIsland());
                }

                singleNeighbors.removeAll(existingNeighbors);
                if (singleNeighbors.isEmpty()) {
                    for (Island neighbor : doubleNeighbors) {
                        // Only add a bridge, if neither double nor single exists.
                        Bridge doubleBridge = new Bridge(island, neighbor, true);
                        Bridge bridge = new Bridge(island, neighbor, false);
                        if (!existing.contains(bridge) && !existing.contains(doubleBridge))
                            return bridge;
                    }
                }
            }
        }

        return null;
    }
}

/**
 * A solve algorithm which makes use of the fact, that
 * all islands must be connected (that is: none must be
 * isolated).
 *
 * @author Maik Messerschmidt
 */
class Isolated implements Algorithm {
    /**
     * Return a possible bridge for the given board or null.
     *
     * @param board
     * @return A possible bridge.
     */
    public Bridge nextBridge(Board board) {
        /*
         * An island can be can be isolated, if it
         * has <= 2 required bridges <= 2 and neighbors
         * with the same number of required bridges.
         *
         * In this case, we can infer the need for a
         * bridge to another neighbor if the number of
         * neighbors is greater 1. The choice of this other
         * bridge is unambiguous, if the number of
         * neighbors is exactly 2.
         */
        for (Island island : board.getIslands()) {
            int required = island.getRequiredBridges();
            if (required > 2)
                continue;

            /*
             * Count existing bridges and skip this island,
             * if it already has enough. The island may have
             * to many bridges already, if the user made a
             * mistake.
             */
            List<Bridge> existing = board.bridges(island);
            if (required <= Bridge.count(existing))
                continue;

            List<Island> neighbors = board.neighbors(island);
            if (neighbors.size() != 2)
                continue;

            Island otherIsolated = null;
            for (Island neighbor : neighbors) {
                if (neighbor.getRequiredBridges() == required) {
                    otherIsolated = neighbor;
                    break;
                }
            }

            // Continue with next island, if no
            // isolated neighbor was found.
            if (otherIsolated == null)
                continue;

            // The 'good' neighbor is the one with another index.
            int goodIndex = (neighbors.indexOf(otherIsolated) + 1) % 2;
            Island goodNeighbor = neighbors.get(goodIndex);

            // Build a new bridge, if this bridge (or a double one)
            // doesn't already exist.
            Bridge bridge = new Bridge(island, goodNeighbor, false);

            if (!bridge.isCovered(existing))
                return bridge;
        }
        return null;
    }
}

/**
 * A solve algorithm, which simply tries all combinations
 * of possible bridges, until a solution is found.
 *
 * @author Maik Messerschmidt
 */
class Bruteforce implements Algorithm {
    /**
     * Return a possible bridge for the given board or null.
     *
     * @param board
     * @return A possible bridge.
     */
    public Bridge nextBridge(Board board) {
        Board solution = board.copy();
        if (doBruteforce(solution) == true) {
            List<Bridge> existing = board.bridges();
            for (Bridge bridge : solution.bridges()) {
                if (!existing.contains(bridge))
                    return bridge;
            }
        }
        return null;
    }

    /**
     * Run brute force in place on the given board and return the success status.
     *
     * @param board - the board to solve.
     * @return true, if a possible solution has been found and applied, false otherwise.
     */
    private boolean doBruteforce(Board board) {
        List<Board> solutions = new ArrayList<Board>();
        List<Island> incomplete = board.getIncomplete();

        // Found a solution
        if (incomplete.isEmpty()) {
            if (board.isComplete())
                return true;
            else
                return false;
        }

        // Try all possible bridges for all valid
        // neighbors of the first island.
        Island island = incomplete.get(0);
        List<Bridge> existing = board.bridges(island);
        List<Island> neighbors = board.validNeighbors(island);

        for (Island neighbor : neighbors) {
            // Skip islands, which already have a double bridge
            if (existing.contains(new Bridge(island, neighbor, true)))
                continue;

                // Try a double bridge, if this island already has a bridge
                // and recurse into doBruteforce, if it worked.
            else if (existing.contains(new Bridge(island, neighbor, false))) {
                try {
                    Bridge bridge = new Bridge(island, neighbor, true);
                    board.addBridge(bridge);
                    if (doBruteforce(board) == true)
                        return true;
                    board.removeOneBridge(bridge);
                } catch (IllegalArgumentException e) {
                    continue;
                }
            } else {
                // Try a single bridge, if this island has no bridge
                // and recurse into doBruteforce, if it worked.
                try {
                    Bridge bridge = new Bridge(island, neighbor, false);
                    board.addBridge(bridge);
                    if (doBruteforce(board) == true)
                        return true;
                    board.removeOneBridge(bridge);
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        }

        return false;
    }
}

/**
 * A solver for bridges game boards.
 * <br><br>
 * Execute a single solve step with:
 * <code>boolean success = BoardSolver.step(board);</code><br>
 * Completely solve the board (if possible) with:
 * <code>BoardSolver.solve(board);</code>
 *
 * @author Maik Messerschmidt
 */
public class BoardSolver {
    final private static List<Algorithm> ALGORITHMS = Arrays.asList(
            new Required(),
            new Isolated(),
            new Bruteforce()
    );

    /**
     * Check, if there exists a possible solve step for the given board.
     *
     * @param board - the board to be solved.
     * @return true, if a step exists, false otherwise.
     */
    public static boolean hasStep(Board board) {
        return nextBridge(board) != null;
    }

    /**
     * Executes a single solve step on the given board _inplace_.
     *
     * @param board The board to be solved.
     * @return true, if the board changed, false otherwise.
     */
    public static boolean step(Board board) {
        Bridge bridge = nextBridge(board);
        if (bridge == null)
            return false;
        else {
            board.addBridge(bridge);
            return true;
        }
    }

    /**
     * Return the next possible bridge for a given board.
     *
     * @param board - the board to be solved.
     * @return A bridge, which can be added to the board or null.
     */
    public static Bridge nextBridge(Board board) {
        for (Algorithm algorithm : ALGORITHMS) {
            Bridge bridge = algorithm.nextBridge(board);
            if (bridge != null)
                return bridge;
        }
        return null;
    }

    /**
     * Solve the board (as good as we can) _inplace_.
     *
     * @param board - The board to be solved.
     */
    public static void solve(Board board) {
        boolean changed;
        do {
            changed = step(board);
        } while (changed);
    }
}
