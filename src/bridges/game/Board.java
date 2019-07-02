package bridges.game;

import java.lang.Math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Represents a board in a bridges game.
 *
 * @author Maik Messerschmidt
 */
public class Board {
    final private List<Island> islands;
    final private List<Bridge> bridges;
    final private int width;
    final private int height;

    /**
     * Creates a new Board instance.
     *
     * @param width  - the (fixed) width of the board.
     * @param height - the (fixed) height of the board.
     * @throws IllegalArgumentException if width or height are invalid.
     */
    public Board(int width, int height) throws IllegalArgumentException {
        this(width, height, null, null);
    }

    /**
     * Creates a new Board instance.
     *
     * @param width   - the (fixed) width of the board.
     * @param height  - the (fixed) height of the board.
     * @param islands - a list of islands to be added to the board.
     * @param bridges - a list of bridges to be added to the board.
     * @throws IllegalArgumentException if width or height are invalid or if the islands or bridges have invalid coordinates.
     */
    public Board(int width, int height, List<Island> islands, List<Bridge> bridges) throws IllegalArgumentException {
        if (width < 1 || height < 1)
            throw new IllegalArgumentException("Board width and height must be >= 1.");

        this.width = width;
        this.height = height;

        this.islands = new ArrayList<Island>();
        this.bridges = new ArrayList<Bridge>();

        if (islands != null) {
            for (Island island : islands)
                addIsland(island);
        }

        if (bridges != null) {
            for (Bridge bridge : bridges)
                addBridge(bridge);
        }
    }

    /**
     * Return a copy of this board.
     *
     * @return Board instance
     */
    public Board copy() {
        return new Board(width, height, new ArrayList<Island>(islands), new ArrayList<Bridge>(bridges));
    }

    /**
     * @return The width of this board.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return The height of this board.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return The current island count of this board.
     */
    public int getIslandCount() {
        return islands.size();
    }

    /**
     * Adds an island to the board.
     *
     * @param island - the island to be placed on the board.
     * @throws IllegalArgumentException if
     *                                  1. the island doesn't fit onto this board (x or y too large) or
     *                                  2. the island is placed directly next to another or on an island,
     *                                  which is already present on the board or
     */
    public void addIsland(Island island) {
        // Island doesn't fit onto the board.
        if (island.getX() >= width || island.getY() >= height)
            throw new IllegalArgumentException(
                    "Cannot place" + island + ". Board is " + width + " x " + height + ".");

        // Island collides with another already present island.
        for (Island is : islands) {
            if (is.collidesWith(island))
                throw new IllegalArgumentException(
                        island + " collides with already present " + is);
        }

        islands.add(island);
    }

    /**
     * Create a bridge from the given island into the given direction without adding it.
     * <p>
     * This method doesn't check, if the bridge would cause any conflicts when added.
     *
     * @param island the Island to start from.
     * @param dir    the direction to build to.
     * @return The newly created bridge or:
     * null, if there is already a double bridge in this direction.
     * null, if there is no neighbor in the given direction.
     */
    public Bridge createBridge(Island island, Direction dir) {
        Island neighbor = neighbor(island, dir);
        if (neighbor == null)
            return null;

        Bridge other = searchBridge(island, neighbor);
        if (other == null)
            return new Bridge(island, neighbor, false);
        else if (!other.isDouble())
            return new Bridge(island, neighbor, true);
        else
            return null;
    }

    /**
     * Check if the given bridge can be added to the board.
     *
     * @param bridge - the bridge to be checked.
     * @return true, if it can be added, false otherwise.
     */
    public boolean canAdd(Bridge bridge) {
        if (bridge == null)
            return false;

        if (!(islands.contains(bridge.getFirstIsland())
                && islands.contains(bridge.getSecondIsland())))
            return false;

        for (Bridge b : bridges) {
            // reject crossing bridges
            if (b.crosses(bridge))
                return false;
            // reject equal bridges
            else if (b.equals(bridge))
                return false;
        }

        // reject single bridges, if an equivalent double bridge is present.
        if (! bridge.isDouble() && bridge.isCovered(bridges))
            return false;

        return true;
    }

    /**
     * Return all bridges on the board, which connect the given island.
     *
     * @param island - the island which has to be connected.
     * @return All bridges on the board, which connect the given island.
     */
    public List<Bridge> bridges(Island island) {
        List<Bridge> connected = new ArrayList<Bridge>();

        for (Bridge b : bridges) {
            if (b.hasIsland(island))
                connected.add(b);
        }
        return connected;
    }

    /**
     * Return all bridges on the board.
     *
     * @return All bridges on the board.
     */
    public List<Bridge> bridges() {
        return new ArrayList<Bridge>(bridges);
    }

    /**
     * Return the island at the given board position or null.
     *
     * @param x The x coordinate of the island.
     * @param y The y coordinate of the island.
     * @return The island at the given position or null, if no island is present.
     */
    public Island getIslandAt(int x, int y) {
        for (Island island : islands) {
            if (island.getX() == x && island.getY() == y)
                return island;
        }
        return null;
    }

    /**
     * Returns a list of islands on this board.
     * <p>
     * The returned list can be manipulated freely while the
     * board state stays unchanged.
     *
     * @return: A list of all islands on the board.
     */
    public List<Island> getIslands() {
        return new ArrayList<Island>(islands);
    }

    /**
     * Return a bridge, which connects both islands or <code>null</code>.<br>
     * The order of the islands does not matter, so
     * <code>searchBridge(a, b)</code> is the same as <code>searchBridge(b, a)</code>.
     * <br><br>
     * It also doesn't matter, if the island is part of the board. This simply
     * results in returning <code>null</code>.
     *
     * @param island1 - the first island.
     * @param island2 - the second island.
     * @return A bridge, which connects both islands or null.
     */
    public Bridge searchBridge(Island island1, Island island2) {
        for (Bridge b : bridges) {
            if (b.hasIsland(island1) && b.hasIsland(island2))
                return b;
        }
        return null;
    }

    /**
     * Return a bridge from the given island in the given direction or <code>null</code>.
     *
     * @param island - the island to start from.
     * @param dir    - the direction to go into.
     * @return A bridge with the given criteria or null.
     */
    public Bridge searchBridge(Island island, Direction dir) {
        Island neighbor = neighbor(island, dir);
        if (neighbor == null)
            return null;
        return searchBridge(island, neighbor);
    }

    /**
     * Adds the given bridge to the board, if possible.
     *
     * <br><br>
     * Note: This allows to add bridges, which cross other
     * bridges already on the board.
     *
     * @param bridge - the bridge to be added.
     * @throws IllegalArgumentException if the two islands of the bridge aren't neighbors or
     *                                  the bridge cannot replace an already existing bridge
     *                                  (e.g. trying to replace a double bridge with a single).
     */
    public void addBridge(Bridge bridge) throws IllegalArgumentException {
        Island first = bridge.getFirstIsland();
        Island second = bridge.getSecondIsland();

        if (!neighbors(first).contains(second))
            throw new IllegalArgumentException(
                    "Cannot add bridge " + bridge + "." +
                            "Contained islands aren't neighbors.");

        Bridge other = searchBridge(first, second);

        // Add the new bridge (single or double)
        if (other == null)
            bridges.add(bridge);
            // Replace the old single bridge with a double bridge.
        else {
            if (!other.isDouble() && bridge.isDouble()) {
                bridges.remove(other);
                bridges.add(bridge);
            } else
                throw new IllegalArgumentException("Cannot replace " + other + " with " + bridge);
        }
    }

    /**
     * Remove the given bridge or fail.
     * <br><br>
     * Note: This will replace double bridges on the board with single bridges.
     *
     * @param bridge - the bridge to remove.
     * @return The bridge, which replaced the old one (if it was a double bridge) or null.
     * @throws IllegalArgumentException if the bridge isn't part of this board.
     */
    public Bridge removeOneBridge(Bridge bridge) throws IllegalArgumentException {
        if (!bridges.contains(bridge))
            throw new IllegalArgumentException("Board doesn't contain " + bridge + ".");

        bridges.remove(bridge);
        if (bridge.isDouble()) {
            Bridge newBridge = new Bridge(bridge.getFirstIsland(), bridge.getSecondIsland(), false);
            bridges.add(newBridge);
            return newBridge;
        } else
            return null;
    }

    /**
     * Return the neighbor of this island in the given direction or null.
     * The result of the method does not depend on the bridges on the board.
     *
     * @param island    - island to find a neighbor for.
     * @param direction - Direction, in which a neighbor is looked for.
     * @return the island next to this one in the given direction or null.
     * @throws IllegalArgumentException if the island is not present on this board.
     */
    public Island neighbor(Island island, Direction direction) throws IllegalArgumentException {
        if (!islands.contains(island))
            throw new IllegalArgumentException("Island not present on board.");

        Island neighbor = null;

        for (Island other : islands) {
            /*
             * The other island lies in the given direction if and only if the
             * signum of the x- and y-deltas match.
             * E.g. a island to the south has dx=0 and dy=3, so the signums
             * match these of SOUTH.dx (==0) and SOUTH.dy (==1).
             *
             * Note: skipping the current island is not needed, since dx==0, dy==0
             * doesn't match any direction (and the performance gain would be minimal).
             */
            int dx = other.getX() - island.getX();
            int dy = other.getY() - island.getY();
            if (Math.signum(dx) == Math.signum(direction.dx) && Math.signum(dy) == Math.signum(direction.dy)) {
                /*
                 * Found a candidate for a neighbor.
                 * Set this to the new neighbor, if neighbor is not set yet or
                 * if the distances of this island is less than the distance of the
                 * other neighbor candidate.
                 */
                if (neighbor == null)
                    neighbor = other;
                else if (Math.abs(neighbor.getX() - island.getX()) > Math.abs(dx))
                    neighbor = other;
                else if (Math.abs(neighbor.getY() - island.getY()) > Math.abs(dy))
                    neighbor = other;
            }
        }

        return neighbor;
    }

    /**
     * Return a list of all neighbors of the given island on the board.
     * The result of this method does not depend on the bridges on the board.
     *
     * @param island The island for which we return the neighbors.
     * @return List of all neighbors.
     * @throws IllegalArgumentException if island is null.
     */
    public List<Island> neighbors(Island island) throws IllegalArgumentException {
        if (island == null)
            throw new IllegalArgumentException("Island required, got null.");

        List<Island> neighbors = new ArrayList<Island>();
        Island neighbor;

        for (Direction dir : Direction.values()) {
            if ((neighbor = neighbor(island, dir)) != null)
                neighbors.add(neighbor);
        }
        return neighbors;
    }

    /**
     * Return a list of all neighbors, where we can actually build bridges to.
     * The result of this method depends on the bridges, that are present on the board.
     *
     * @param island The island for which we return the neighbors.
     * @return A list of all valid neighbors of the island.
     * @throws IllegalArgumentException passed on from getAllNeighbors().
     */
    public List<Island> validNeighbors(Island island) throws IllegalArgumentException {
        List<Island> validNeighbors = new ArrayList<Island>();
        for (Island other : neighbors(island)) {
            boolean isValid = true;
            Bridge testBridge = new Bridge(island, other, false);
            for (Bridge b : bridges) {
                if (testBridge.crosses(b)) {
                    isValid = false;
                    break;
                }
            }
            if (isValid)
                validNeighbors.add(other);
        }
        return validNeighbors;
    }

    /**
     * Return a list of islands, which have less bridges than required.
     *
     * @return The list of islands.
     */
    public List<Island> getIncomplete() {
        List<Island> incomplete = new ArrayList<Island>();

        for (Island island : islands) {
            int required = island.getRequiredBridges();
            int count = Bridge.count(bridges(island));
            if (count < required)
                incomplete.add(island);
        }

        return incomplete;
    }

    /**
     * Check, if this board is complete.
     *
     * @return True, if all islands have exactly the required count of bridges
     * and the board is fully connected, false otherwise.
     */
    public boolean isComplete() {
        for (Island island : islands) {
            if (Bridge.count(bridges(island)) != island.getRequiredBridges())
                return false;
        }
        return isFullyConnected();
    }

    /**
     * Check, if the islands on the board are fully connected, that
     * is, every island can be reached from any other island
     * by walking along the bridges on the board.
     *
     * @return true, if every island can be reached, false otherwise.
     */
    public boolean isFullyConnected() {
        return partition().size() == 1;
    }

    /**
     * Partitions the board into groups of connected islands.
     * <br><br>
     * For a board without any bridges one gets: <code>partition().size() == getIslandCount()</code>.<br>
     * For a board that is fully conneted one gets: <code>partition().size() == 1</code>.
     *
     * @return A list of list of islands, which are connected.
     */
    public List<List<Island>> partition() {
        List<List<Island>> partitions = new ArrayList<List<Island>>();
        List<Island> unvisited = new ArrayList<Island>(islands);

        while (!unvisited.isEmpty()) {
            partitions.add(walk(unvisited.get(0), unvisited));
        }
        return partitions;
    }

    /**
     * Walk along the bridges on the board only entering unvisited islands returning all visited islands.
     * <br/><br/>
     * This method is used to find all islands connected to the current island.
     *
     * @param current   - The island to start walking from.
     * @param unvisited - A list of islands, which can be entered.
     * @return A list of all islands, which where visited by walking along the board.
     */
    private List<Island> walk(Island current, List<Island> unvisited) {
        List<Island> visited = new ArrayList<Island>();
        unvisited.remove(current);
        visited.add(current);

        for (Bridge bridge : bridges(current)) {
            Island other;
            if (bridge.getFirstIsland().equals(current))
                other = bridge.getSecondIsland();
            else
                other = bridge.getFirstIsland();

            if (unvisited.contains(other))
                visited.addAll(walk(other, unvisited));
        }
        return visited;
    }

    /**
     * Resets the game (that is: removes all bridges on the board).
     */
    public void reset() {
        bridges.removeAll(bridges);
    }
}
