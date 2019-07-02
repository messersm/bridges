package bridges.game.tests;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bridges.game.Board;
import bridges.game.Bridge;
import bridges.game.Direction;
import bridges.game.Island;


public class BoardTests {
    @Test
    // A new board has now islands and no bridges
    public void testEmptyBoard() {
        Board board = new Board(5, 10);
        Assert.assertEquals(new ArrayList<Island>(), board.getIslands());
        Assert.assertEquals(0, board.getIslandCount());
        Assert.assertEquals(new ArrayList<Bridge>(), board.bridges());
    }

    @Test(expected = IllegalArgumentException.class)
    // Trying to create a board with an invalid width
    // raises an IllegalArgumentException
    public void testInvalidWidth() {
        Board board = new Board(-1, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    // Trying to create a board with an invalid height
    // raises an IllegalArgumentException
    public void testInvalidHeight() {
        Board board = new Board(3, -1);
    }

    @Test
    public void testGetNeighbour() {
        Board board = new Board(10, 10);
        Island island = new Island(3, 3, 1);
        Island islandNorth = new Island(3, 1, 1);
        Island islandSouth = new Island(3, 9, 1);
        Island islandSouthCloser = new Island(3, 7, 1);
        Island islandWest = new Island(0, 3, 1);
        Island islandEast = new Island(8, 3, 1);
        board.addIsland(island);
        board.addIsland(islandNorth);
        board.addIsland(islandSouth);
        board.addIsland(islandSouthCloser);
        board.addIsland(islandWest);
        board.addIsland(islandEast);
        Assert.assertEquals(islandNorth, board.neighbor(island, Direction.NORTH));
        Assert.assertEquals(islandSouthCloser, board.neighbor(island, Direction.SOUTH));
        Assert.assertEquals(islandWest, board.neighbor(island, Direction.WEST));
        Assert.assertEquals(islandEast, board.neighbor(island, Direction.EAST));
        Assert.assertEquals(null, board.neighbor(islandNorth, Direction.NORTH));
    }
}
