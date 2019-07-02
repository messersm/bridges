package bridges.game;

import java.util.List;
import java.util.Objects;

/**
 * Represent an island in the game Bridges.
 * <p>
 * Defined as immutable object, following:
 * https://docs.oracle.com/javase/tutorial/essential/concurrency/imstrat.html
 *
 * @author Maik Messerschmidt
 */
final public class Island implements Comparable<Island> {
    private final int requiredBridges;
    private final int x;
    private final int y;

    /**
     * Create a new Island instance.
     *
     * @param x               - the x coordinate of the island (greater or equal to 0).
     * @param y               - the y coordinate of the island (greater or equal to 0).
     * @param requiredBridges - the required bridges of the island (must be within the range of 1 to 8 including)
     * @throws IllegalArgumentException if the coordinates or the required bridge count are invalid.
     */
    public Island(int x, int y, int requiredBridges) throws IllegalArgumentException {
        if (x < 0 || y < 0)
            throw new IllegalArgumentException("Island coordinates must be >= 0.");
        if (requiredBridges < 1 || requiredBridges > 8)
            throw new IllegalArgumentException("Island bridge count must be between 1 and 8 (including).");

        this.x = x;
        this.y = y;
        this.requiredBridges = requiredBridges;
    }

    /**
     * @return The required bridge count.
     */
    public int getRequiredBridges() {
        return requiredBridges;
    }

    /**
     * @return The x coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * @return The y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * @return The island as a String (used for debugging only).
     */
    @Override
    public String toString() {
        return "Island(" + x + ", " + y + ", " + requiredBridges + ")";
    }

    /**
     * Check, if this island collides with another (that is: they are too close).
     *
     * @param other - the other island.
     * @return true, if both islands have identically coordinates or lie next to each other, false otherwise.
     */
    public boolean collidesWith(Island other) {
        int dx = other.getX() - x;
        int dy = other.getY() - y;
        return (dx == 0 && Math.abs(dy) <= 1 || dy == 0 && Math.abs(dx) <= 1);
    }

    /**
     * Check, if the other object is equal to this island.
     *
     * @return true, if the other object is and Island and has the same coordinates
     * and the same number of required bridges, false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Island)
            return equals((Island) other);
        else
            return false;
    }

    /**
     * Check, if the other island is equal to this island.
     *
     * @param other The other island.
     * @return true, if the other island has the same coordinates
     * and the same number of required bridges, false otherwise.
     */
    public boolean equals(Island other) {
        if (other == null)
            return false;
        else
            return compareTo(other) == 0;
    }

    /**
     * @return A hashCode for this island instance.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y, requiredBridges);
    }

    /**
     * Compare this island to another.
     * <br><br>
     * An island is considered less than another, if<br>
     * - the x coordinate is less,<br>
     * - the y coordinate is less,<br>
     * - the count of required bridges is less (in that order).<br>
     * <br><br>
     * If all are the same it is equal and greater otherwise.
     */
    @Override
    public int compareTo(Island other) {
        if (x < other.getX())
            return -1;
        else if (x > other.getX())
            return 1;
        else if (y < other.getY())
            return -1;
        else if (y > other.getY())
            return 1;
        else if (requiredBridges < other.getRequiredBridges())
            return -1;
        else if (requiredBridges > other.getRequiredBridges())
            return 1;
        else
            return 0;
    }

    ;

    /**
     * Check, if this island is cut by the given bridges.
     *
     * @param bridges - The bridges to check, if they cut this island.
     * @return true, if any of the given bridges crosses (or covers) the coordinates of this island.
     */
    public boolean isCutBy(List<Bridge> bridges) {
        for (Bridge bridge : bridges) {
            if (bridge.cuts(this))
                return true;
        }
        return false;
    }
}
