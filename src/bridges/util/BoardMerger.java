package bridges.util;

import bridges.game.Board;
import bridges.game.Bridge;
import bridges.game.Island;

/**
 * Class that merges two given bridge puzzles.
 *
 * @author Maik Messerschmidt
 */
public class BoardMerger {

    /**
     * Merge the to given boards.
     * <p>
     * Note: If one of the boards doesn't have islands, a copy of the other is returned.
     *
     * @param left       The board to be placed on the left.
     * @param right      The board to be placed on the right.
     * @param addBridges Whether to add bridges, that are already present on the given boards.
     * @return The new board.
     */
    public static Board merge(Board left, Board right, Boolean addBridges) {
        /*
         * We connect the top right island of the left board with
         * the top left island of the right board (we call them "gates").
         * These islands are guaranteed to have less than 8 bridges.
         *
         * The dimensions of the new board are then given by:
         * 		width = b_l + b_r + 1 and
         * 		height = max(h_l, h_r) + abs(dx)
         *
         * Where dx is the difference of the x-coordinates of the islands
         * mentioned above.
         */

        Island leftGate = findTopRight(left);
        Island rightGate = findTopLeft(right);

        if (leftGate == null)
            return right.copy();
        if (rightGate == null)
            return left.copy();

        int leftDx = 0;
        int rightDx = left.getWidth() + 1;
        int dx = rightDx;

        int rightDy;
        int leftDy;
        int dy;

        if (leftGate.getY() < rightGate.getY()) {
            leftDy = 0;
            rightDy = leftGate.getY() - rightGate.getY();
            dy = rightDy;
        } else {
            leftDy = rightGate.getY() - leftGate.getY();
            rightDy = 0;
            dy = leftDy;
        }

        int width = left.getWidth() + right.getWidth() + 1;
        int height = Math.max(left.getHeight(), right.getHeight()) + Math.abs(dy);

        Board board = new Board(width, height);

        // Add all islands of the left board, replacing the left gate with
        // an island, that has one more bridge.
        for (Island island : left.getIslands())
            board.addIsland(translateIsland(island, leftDx, leftDy, leftGate));

        // Add all islands of the right board, replacing the right gate with
        // an island, that has one more bridge.
        for (Island island : right.getIslands())
            board.addIsland(translateIsland(island, rightDx, rightDy, rightGate));

        // If bridges should not be added, return the board now.
        if (!addBridges)
            return board;

        // Add bridges

        // Add all bridges, that are already present in the left board,
        // but replace the gate with the correct new island.
        for (Bridge bridge : left.bridges())
            board.addBridge(translateBridge(bridge, leftDx, leftDy, leftGate));

        // Add all bridges, that are already present in the right board,
        // translated by (dx, dy) and replace the gate with the correct new island.
        for (Bridge bridge : right.bridges())
            board.addBridge(translateBridge(bridge, rightDx, rightDy, rightGate));

        return board;
    }


    /**
     * Return a new island with translated coordinates and changed bridgeCount, if the island is a gate.
     *
     * @param island The island to change.
     * @param dx     The difference of the x-coordinates.
     * @param dy     The difference of the y-coordinates.
     * @param gate   The gate of the board, that is handled.
     * @return The new Island with changed coordinates.
     */
    private static Island translateIsland(Island island, int dx, int dy, Island gate) {
        int x = island.getX() + dx;
        int y = island.getY() + dy;
        int bridgeCount = island.getRequiredBridges();

        if (island.equals(gate))
            bridgeCount += 1;

        return new Island(x, y, bridgeCount);
    }

    /**
     * Return a new bridge with the translated coordinates and changed bridgeCount
     * for the first and second island.
     *
     * @param bridge The bridge to be changed.
     * @param dx     The difference of the x-coordinates.
     * @param dy     The difference of the y-coordinates.
     * @param gate   The gate of the board, that is handled.
     * @return The new Island with changed coordinates.
     */
    private static Bridge translateBridge(Bridge bridge, int dx, int dy, Island gate) {
        Island first = translateIsland(bridge.getFirstIsland(), dx, dy, gate);
        Island second = translateIsland(bridge.getSecondIsland(), dx, dy, gate);
        return new Bridge(first, second, bridge.isDouble());
    }

    /**
     * Return the top right island of the board (or null, if it has no islands).
     *
     * @param board The board to find the top-left island in.
     * @return The top-right island (or null).
     */
    private static Island findTopRight(Board board) {
        Island topleft = null;

        for (Island island : board.getIslands()) {
            if (topleft == null) {
                topleft = island;
                continue;
            }
            if (island.getY() < topleft.getY()) {
                topleft = island;
            } else if (island.getY() == topleft.getY() && island.getX() > topleft.getX()) {
                topleft = island;
            }
        }
        return topleft;
    }

    /**
     * Return the top left island of the board (or null, if it has no islands).
     *
     * @param board The board to find the top-left island in.
     * @return The top-left island (or null).
     */
    private static Island findTopLeft(Board board) {
        Island topright = null;

        for (Island island : board.getIslands()) {
            if (topright == null) {
                topright = island;
                continue;
            }
            if (island.getY() < topright.getY()) {
                topright = island;
            } else if (island.getY() == topright.getY() && island.getX() < topright.getX()) {
                topright = island;
            }
        }
        return topright;
    }
}
