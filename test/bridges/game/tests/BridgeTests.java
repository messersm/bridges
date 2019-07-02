package bridges.game.tests;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
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

public class BridgeTests {

    @Test
    public void testHasIsland() {
        Island a = new Island(0, 2, 3);
        Island b = new Island(0, 2, 5);
        Island c = new Island(1, 1, 2);
        Island d = new Island(0, 2, 3);

        Bridge bridge = new Bridge(a, b, false);
        assertEquals(true, bridge.hasIsland(a));
        assertEquals(true, bridge.hasIsland(b));
        assertEquals(false, bridge.hasIsland(c));
        assertEquals(true, bridge.hasIsland(d));
        assertEquals(false, bridge.hasIsland(null));
    }

    /**
     * A bridge equals another, if the other
     * has the same island objects and the same isDouble
     * value, even if the order of the islands
     * is different.
     */
    @Test
    public void testEqualsOrder() {
        Island i1 = new Island(0, 2, 3);
        Island i2 = new Island(0, 2, 5);
        Bridge b1 = new Bridge(i1, i2, false);
        Bridge b2 = new Bridge(i2, i1, false);
        Bridge b3 = new Bridge(i1, i2, true);
        Bridge b4 = new Bridge(i2, i1, true);

        // equal: b1-b2, b3-b4
        assertEquals(b1, b2);
        assertNotEquals(b1, b3);
        assertNotEquals(b1, b4);
        assertEquals(b2, b1);
        assertNotEquals(b2, b3);
        assertNotEquals(b2, b4);
        assertNotEquals(b3, b1);
        assertNotEquals(b3, b2);
        assertEquals(b3, b4);
        assertNotEquals(b4, b1);
        assertNotEquals(b4, b2);
        assertEquals(b4, b3);
    }

    /**
     * A bridge equals another, if the other
     * has equal islands, even if these
     * islands aren't the same object.
     */
    @Test
    public void testEqualsSameIslands() {
        Island i1 = new Island(0, 2, 3);
        Island i2 = new Island(0, 2, 5);
        Island i3 = new Island(0, 2, 3);

        // equal: b1-b3
        Bridge b1 = new Bridge(i1, i2, false);
        Bridge b2 = new Bridge(i1, i3, false);
        Bridge b3 = new Bridge(i2, i3, false);
        assertNotEquals(b1, b2);
        assertEquals(b1, b3);
        assertNotEquals(b2, b3);
    }

    /**
     * A bridge can only be build between islands
     * of matching x or y coordinates.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidIslands() {
        new Bridge(new Island(0, 3, 2), new Island(1, 1, 3), false);
    }

    /**
     * A list contains a bridge, if an equal one is
     * present in the list.
     */
    @Test
    public void testContains() {
        List<Bridge> bridges = new ArrayList<Bridge>();

        Bridge a = new Bridge(new Island(0, 3, 2), new Island(0, 1, 3), false);
        Bridge b = new Bridge(new Island(0, 3, 2), new Island(0, 1, 2), false); // false
        Bridge c = new Bridge(new Island(0, 3, 2), new Island(0, 1, 3), true); // false
        Bridge d = new Bridge(new Island(0, 3, 2), new Island(0, 1, 3), false); // true
        Bridge e = new Bridge(new Island(0, 1, 3), new Island(0, 3, 2), false); // true

        bridges.add(a);
        assertEquals(false, bridges.contains(b));
        assertEquals(false, bridges.contains(c));
        assertEquals(true, bridges.contains(d));
        assertEquals(true, bridges.contains(e));
    }

    @Test
    public void testCrosses() {
        /*
         *   012345678
         * 0   a
         * 1 b-+---c
         * 2   |   |
         * 3   |   |
         * 4   d---+-e
         * 5      f|
         * 6     g+h
         * 7      i
         */

        Island a = new Island(2, 0, 1);
        Island b = new Island(0, 1, 1);
        Island c = new Island(6, 1, 1);
        Island d = new Island(2, 4, 1);
        Island e = new Island(8, 4, 1);
        Island f = new Island(5, 5, 1);
        Island g = new Island(4, 6, 1);
        Island h = new Island(6, 6, 1);
        Island i = new Island(5, 7, 1);

        Island[][] allBridges = {
                {a, d},
                {b, c},
                {c, h},
                {d, e},
                {f, i},
                {g, h}
        };

        Island[][] crossing_arr = {
                {a, d, b, c},
                {c, h, d, e},
                {f, i, g, h}
        };

        ArrayList<Set<Island>> crossing = new ArrayList<Set<Island>>();
        for (Island[] arr : crossing_arr)
            crossing.add(new HashSet<Island>(Arrays.asList(arr)));

        for (Island[] b1 : allBridges) {
            for (Island[] b2 : allBridges) {
                // skip equal bridges
                if (b1.equals(b2))
                    continue;

                Bridge bridge1 = new Bridge(b1[0], b1[1], false);
                Bridge bridge2 = new Bridge(b2[0], b2[1], false);
                Set<Island> islands = new HashSet<Island>();
                islands.add(bridge1.getFirstIsland());
                islands.add(bridge1.getSecondIsland());
                islands.add(bridge2.getFirstIsland());
                islands.add(bridge2.getSecondIsland());

                if (crossing.contains(islands)) {
                    System.out.println("Must cross: " + bridge1 + " " + bridge2);
                    assertEquals(true, bridge1.crosses(bridge2));
                    assertEquals(true, bridge2.crosses(bridge1));
                } else {
                    System.out.println("Must not cross: " + bridge1 + " " + bridge2);
                    assertEquals(false, bridge1.crosses(bridge2));
                    assertEquals(false, bridge2.crosses(bridge1));
                }
            }
        }
    }
}
