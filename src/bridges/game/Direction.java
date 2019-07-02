package bridges.game;

/**
 * Represents a geographic direction on the board, using
 * coordinate system of the specification (that is: North is negative y).
 *
 * @author Maik Messerschmidt
 */
public enum Direction {
    EAST(1, 0), SOUTH(0, 1), WEST(-1, 0), NORTH(0, -1);

    public final int dx;
    public final int dy;

    /**
     * @param dx - the x direction of the geographic direction.
     * @param dy - the y direction of the geographic direction.
     */
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Returns the direction which is nearest to (dx, dy).
     * E.g. (2, -3) lies more to the north, so we return Direction.NORTH.
     *
     * @param dx - the x direction of the geographic direction.
     * @param dy - the y direction of the geographic direction.
     * @return A direction, which best matches the given deltas.
     */
    public static Direction nearest(int dx, int dy) {
        int sx = 0;
        int sy = 0;

        if (Math.abs(dx) > Math.abs(dy)) {
            sx = (int) Math.signum(dx);
            sy = 0;
        } else if (Math.abs(dy) > Math.abs(dx)) {
            sx = 0;
            sy = (int) Math.signum(dy);
        } else
            return null;

        for (Direction dir : Direction.values()) {
            if (sx == dir.dx && sy == dir.dy)
                return dir;
        }
        return null;
    }
}
