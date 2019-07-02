package bridges.game.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import bridges.game.Board;
import bridges.game.Bridge;
import bridges.game.Direction;
import bridges.game.Island;

public class IslandTests {
    @Test
    // Test, if HashCode() follows the hashCode Contract.
    public void testHashCodeContract() {
        List<Island> islands = new ArrayList<Island>();

        for (int x = 0; x < 10; x++)
            for (int y = 0; y < 10; y++)
                for (int br = 1; br <= 8; br++)
                    islands.add(new Island(x, y, br));

        // hashCode() returns the same value
        // for the objects with the same data.
        for (Island a : islands) {
            int x = a.getX();
            int y = a.getY();
            int br = a.getRequiredBridges();
            Island b = new Island(x, y, br);
            assertEquals(a.hashCode(), b.hashCode());
        }

        // If equals() is true, the hashCode must be the same.
        for (Island a : islands)
            for (Island b : islands)
                if (a.equals(b))
                    assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    // Test, if equals() works correctly.
    public void testEquals() {
        List<Island> islands = new ArrayList<Island>();

        for (int x = 0; x < 10; x++)
            for (int y = 0; y < 10; y++)
                for (int br = 1; br <= 8; br++)
                    islands.add(new Island(x, y, br));

        for (Island i1 : islands) {
            for (Island i2 : islands) {
                int x1 = i1.getX();
                int y1 = i1.getY();
                int br1 = i1.getRequiredBridges();
                int x2 = i2.getX();
                int y2 = i2.getY();
                int br2 = i2.getRequiredBridges();

                if (x1 == x2 && y1 == y2 && br1 == br2) {
                    assertEquals(true, i1.equals(i2));
                    assertEquals(true, i2.equals(i1));
                } else {
                    assertEquals(false, i1.equals(i2));
                    assertEquals(false, i2.equals(i1));
                }
            }
        }
    }

    @Test
    public void testSimpleEquals() {
        Island i1 = new Island(0, 0, 1);
        Island i2 = new Island(0, 1, 1);
        assertEquals(false, i1.equals(i2));
    }
}
