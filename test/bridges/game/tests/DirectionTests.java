package bridges.game.tests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import bridges.game.Direction;

public class DirectionTests {

    /**
     * We test these cases:<br/>
     * If dy > 0 and abs(dx) < abs(dy) return SOUTH.<br/>
     * If dy < 0 and abs(dx) < abs(dy) return NORTH.<br/>
     * If dx > 0 and abs(dy) < abs(dx) return EAST.<br/>
     * If dx < 0 and abs(dy) < abs(dx) return WEST.<br/>
     * If abs(dx) == abs(dy) return null.<br/>
     * <br/>
     * We also make sure, that all combinations are covered
     * by these cases.
     */
    @Test
    public void testNearest() {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                if (dy > 0 && Math.abs(dx) < Math.abs(dy))
                    assertEquals(Direction.SOUTH, Direction.nearest(dx, dy));
                else if (dy < 0 && Math.abs(dx) < Math.abs(dy))
                    assertEquals(Direction.NORTH, Direction.nearest(dx, dy));
                else if (dx > 0 && Math.abs(dy) < Math.abs(dx))
                    assertEquals(Direction.EAST, Direction.nearest(dx, dy));
                else if (dx < 0 && Math.abs(dy) < Math.abs(dx))
                    assertEquals(Direction.WEST, Direction.nearest(dx, dy));
                else if (Math.abs(dx) == Math.abs(dy))
                    assertEquals(null, Direction.nearest(dx, dy));
                else
                    fail("Case not covered: dx=" + dx + ", dy=" + dy + ".");
            }
        }
    }
}
