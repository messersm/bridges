package bridges.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represent a bridge between two islands in the game Bridges.
 * <p>
 * Defined as immutable object, following:
 * https://docs.oracle.com/javase/tutorial/essential/concurrency/imstrat.html
 *
 * @author Maik Messerschmidt
 */
final public class Bridge implements Comparable<Bridge> {
    final private Island first;
    final private Island second;
    final private boolean isDouble;

    /**
     * Create a new Bridge instance.
     *
     * @param island1  - The first island of the bridge.
     * @param island2  - The second island of the birdge.
     * @param isDouble - Whether or not the bridge is a double bridge.
     * @throws IllegalArgumentException if one of the islands is null or the islands don't match in either x or y coordinate.
     */
    public Bridge(Island island1, Island island2, boolean isDouble) throws IllegalArgumentException {
        if (island1 == null || island2 == null)
            throw new IllegalArgumentException("Bridge constructor needs two Islands, not null.");

        if (island1.getX() != island2.getX() && island1.getY() != island2.getY())
            throw new IllegalArgumentException("Islands must match in x or y coordinate.");

        this.isDouble = isDouble;
        this.first = island1;
        this.second = island2;
    }

    /**
     * @return The first island of the bridge.
     */
    public Island getFirstIsland() {
        return first;
    }

    /**
     * @return The second island of the bridge.
     */
    public Island getSecondIsland() {
        return second;
    }

    /**
     * @return Whether or not this is a double bridge.
     */
    public boolean isDouble() {
        return isDouble;
    }

    /**
     * Check, if the given island is connected by this bridge.
     *
     * @param island the island to check for.
     * @return true, if it is the case, false otherwise.
     */
    public boolean hasIsland(Island island) {
        return first.equals(island) || second.equals(island);
    }

    /**
     * @return The bridge as a String (used for debugging only).
     */
    @Override
    public String toString() {
        return "Bridge(" + first + ", " + second + ", " + isDouble + ")";
    }

    /**
     * @return true, if the other object is a bridge and connects the
     * same islands and is also a double (or a single) bridge,
     * false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Bridge)
            return equals((Bridge) other);
        else
            return false;
    }

    /**
     * Check, if this bridge is equal to another bridge.
     *
     * @param other The other bridge.
     * @return true, if the bridge connects the
     * same islands and is also a double (or a single) bridge,
     * false otherwise.
     */
    public boolean equals(Bridge other) {
        if (other == null)
            return false;
        else
            return compareTo(other) == 0;
    }

    /**
     * @return a hashCode for this bridge.
     */
    @Override
    public int hashCode() {
        Set<Island> islands = new HashSet<Island>(
                Arrays.asList(new Island[]{first, second}));
        return Objects.hash(islands, isDouble);
    }

    /**
     * Return if the other bridge crosses (or covers) this bridge.
     *
     * @param other The other bridge.
     * @return true, if the other bridge crosses this one.
     */
    public boolean crosses(Bridge other) {
        /* case 1:
         *                (x3, y3)
         *                   |
         * (x1, y1) ---------+------- (x2, y2)
         *                   |
         *                   |
         *                (x4, y4)
         *
         *
         * case 2:
         *                (x1, y1)
         *                   |
         * (x3, y3) ---------+------- (x4, y4)
         *                   |
         *                   |
         *                (x2, y2)
         */
        int x1 = first.getX();
        int y1 = first.getY();
        int x2 = second.getX();
        int y2 = second.getY();

        int x3 = other.getFirstIsland().getX();
        int y3 = other.getFirstIsland().getY();
        int x4 = other.getSecondIsland().getX();
        int y4 = other.getSecondIsland().getY();

        return (between(x1, x3, x2) && between(y3, y1, y4)) ||
                (between(x3, x1, x4) && between(y1, y3, y2));
    }

    /**
     * Check, if any of the given bridges crosses this one.
     *
     * @param others A list of other bridges.
     * @return true, if any of the other bridges crosses this bridge, false otherwise.
     */
    public boolean crosses(List<Bridge> others) {
        for (Bridge other : others) {
            if (crosses(other))
                return true;
        }
        return false;
    }

    /**
     * Helper method which checks, if the mid value lies between start and end.
     *
     * @param start - first (or last) value.
     * @param mid   - value, that has to be in the middle of the other two.
     * @param end   - last (or first) value.
     * @return true, if mid lies between start and end (not including).
     */
    private boolean between(int start, int mid, int end) {
        return ((start < mid && mid < end) || (start > mid && mid > end));
    }

    /**
     * Return the total number of bridges in the given list.
     * Double bridges count as two, single bridges count as one.
     *
     * @param bridges The list of bridges to be counted.
     * @return The total number of bridges.
     */
    public static int count(List<Bridge> bridges) {
        int count = 0;
        for (Bridge b : bridges) {
            if (b.isDouble())
                count += 2;
            else
                count += 1;
        }
        return count;
    }

    /**
     * Return if this bridge is covered by the given list
     * (which means a bridge - either single or double - with
     * the same islands exists).
     *
     * @param bridges The list of bridges to search in.
     * @return Whether or not a bridge with the same start and end are present.
     */
    public boolean isCovered(List<Bridge> bridges) {
        if (bridges.contains(this))
            return true;
        else if (bridges.contains(new Bridge(first, second, !isDouble)))
            return true;
        else
            return false;
    }

    /**
     * Compares this bridge to another.
     * <br><br>
     * A bridge is less than another bridge,
     * if is has the first islands within a sorted
     * list of islands of both of these bridges
     * or if the boolean value of isDouble() is less
     * than the boolean value of the other isDouble() value.
     *
     * @return -1, if bridge is smaller than other,
     * 0, if bridge equals other,
     * +1, if bridge is greater than other.
     */
    @Override
    public int compareTo(Bridge other) {
        List<Island> islands = Arrays.asList(new Island[]{
                this.getFirstIsland(),
                this.getSecondIsland(),
                other.getFirstIsland(),
                other.getSecondIsland()
        });
        Collections.sort(islands);
        for (Island island : islands) {
            if (hasIsland(island) && !other.hasIsland(island))
                return -1;
            else if (other.hasIsland(island) && !hasIsland(island))
                return 1;
        }
        return Boolean.compare(isDouble, other.isDouble);
    }

    /**
     * Check, if the given island is cut by this bridge.
     *
     * @param island The island to be checked.
     * @return true, if the island is cut, false otherwise.
     */
    public boolean cuts(Island island) {
        int dx = second.getX() - first.getX();

        if (dx == 0)
            return island.getX() == first.getX()
                    && between(first.getY(), island.getY(), second.getY());
        else
            return island.getY() == first.getY()
                    && between(first.getX(), island.getX(), second.getX());
    }
}
