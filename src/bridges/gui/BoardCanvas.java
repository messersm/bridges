package bridges.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Point;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import bridges.game.Board;
import bridges.game.Island;
import bridges.model.GameModel;
import bridges.game.Bridge;
import bridges.game.Direction;

/**
 * Represents the drawn board for the BridgesApp.
 * <p>
 * Enables the user to interact with the board
 * (build and remove bridges).
 *
 * @author Maik Messerschmidt
 */
public class BoardCanvas extends Canvas implements Observer {
    private GameModel game;
    private int boardWidth = 0;
    private int boardHeight = 0;
    private Bridge selectedBridge = null;
    private Bridge lastBridge = null;
    private boolean displayMissing = false;

    // Colors
    private Color gridColor = Color.LIGHT_GRAY;
    private Color islandColor = Color.GRAY;
    private Color invalidIslandColor = Color.RED;
    private Color completeIslandColor = Color.GREEN;
    private Color textColor = Color.BLACK;
    private Color displayMissingColor = Color.WHITE;
    private Color bridgeColor = Color.BLACK;
    private Color plannedBridgeColor = Color.BLUE;
    private Color lastBridgeColor = Color.GREEN;
    private Color invalidBridgeColor = Color.RED;

    /*
     * Margin to each side in pixel.
     */
    final private int MARGIN = 10;
    private int gridOffsetX = 0;
    private int gridOffsetY = 0;
    private float gridStep = 0;
    private int gridDiameter = 0;
    private int gridTolerance = 0;

    /**
     * Create a new BoardCanvas using the given game model.
     *
     * @param game - the game model to use.
     */
    public BoardCanvas(GameModel game) {
        super();
        this.game = game;
        game.addObserver(this);

        /*
         * Bind calculateGrid() to the resize event of this canvas
         * in order to update the grid, island sizes, etc.
         */
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                BoardCanvas.this.calculateGrid();
            }
        });

        // Enable user to build a bridge by left-clicking.
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    buildBridge(e.getX(), e.getY());
                }
            }
        });

        // Enable user to remove a bridge by right-clicking.
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                    removeBridge(e.getX(), e.getY());
            }
        });

        // Enable user to see a possible bridge.
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                selectBridge(e.getX(), e.getY());
            }
        });
    }

    /**
     * Display a bridge at the given position, if
     * one could be placed.
     *
     * @param mouseX - the x coordinate of the mouse.
     * @param mouseY - the y coordinate of the mouse.
     */
    private void selectBridge(int mouseX, int mouseY) {
        // If a bridge is present select the
        // present bridge. Build a new one otherwise.
        Bridge newBridge = searchBridge(mouseX, mouseY);

        if (newBridge == null)
            newBridge = createBridge(mouseX, mouseY);

        // Repaint the canvas, if the planned bridge changed.
        if (selectedBridge == null) {
            if (newBridge != null) {
                selectedBridge = newBridge;
                repaint();
            }
        } else if (!selectedBridge.equals(newBridge)) {
            selectedBridge = newBridge;
            repaint();
        }
    }

    /**
     * Place a bridge on the board at the given position,
     * if possible.
     *
     * @param mouseX - the x coordinate of the mouse.
     * @param mouseY - the y coordinate of the mouse.
     */
    private void buildBridge(int mouseX, int mouseY) {
        Bridge bridge = createBridge(mouseX, mouseY);
        if (bridge != null) {
            // If we want to allow crossing bridges,
            // the isValid() check must be removed.
            if (game.isValid(bridge)) {
                game.addBridge(bridge);
            }
        }
    }

    /**
     * Remove one bridge at the given mouse position.
     * <br/><br/>
     * This will turn a double bridge into a single
     * bridge and completely remove a single bridge.
     *
     * @param mouseX - the x coordinate of the mouse.
     * @param mouseY - the y coordinate of the mouse.
     */
    private void removeBridge(int mouseX, int mouseY) {
        Bridge bridge = searchBridge(mouseX, mouseY);
        if (bridge != null) {
            game.removeOneBridge(bridge);
        }
    }

    public void displayMissing(boolean doDisplay) {
        displayMissing = doDisplay;
        repaint();
    }

    /**
     * Return an island near the given pixel position or null.
     *
     * @param pixelX - the x coordinate.
     * @param pixelY - the y coordinate.
     * @return The island at the given position or null.
     */
    private Island nearestIsland(int pixelX, int pixelY) {
        Point boardPos = getBoardPosition(pixelX, pixelY);
        return game.getIslandAt((int) boardPos.x, (int) boardPos.y);
    }

    /**
     * Return a bridge, which can be build at the given position.
     * (or null).
     *
     * @param mouseX - the x coordinate of the mouse.
     * @param mouseY - the y coordinate of the mouse.
     * @return A possible bridge at the given position or null.
     */
    private Bridge createBridge(int mouseX, int mouseY) {
        /*
         * 1. If no board is given, return null.
         * 2. Find an island near the given mouse position
         *    or return null.
         * 3. Calculate the direction of the mouse position relative
         *    to the island position or return null.
         * 4. Find a neighboring island in the given direction
         *    or return null.
         * 5. Ensure there can be a bridge between the two islands
         *    added to the board or return null.
         * 6. Return the bridge.
         */
        if (!game.hasBoard())
            return null;
        Island island = nearestIsland(mouseX, mouseY);
        if (island == null)
            return null;
        Point islandPos = getPixelPosition(island.getX(), island.getY());
        Direction dir = getDirection(mouseX, mouseY, (int) islandPos.x, (int) islandPos.y);
        if (dir == null)
            return null;

        return game.createBridge(island, dir);
    }

    /**
     * Return a bridge, which is already built at the given position.
     *
     * @param mouseX - the x coordinate of the mouse.
     * @param mouseY - the y coordinate of the mouse.
     * @return The bridge at the given position or null.
     */
    private Bridge searchBridge(int mouseX, int mouseY) {
        if (!game.hasBoard())
            return null;
        Island island = nearestIsland(mouseX, mouseY);
        if (island == null)
            return null;
        Point islandPos = getPixelPosition(island.getX(), island.getY());
        Direction dir = getDirection(mouseX, mouseY, (int) islandPos.x, (int) islandPos.y);
        if (dir == null)
            return null;

        List<Bridge> bridges = game.getBridges(island, dir);
        if (bridges.isEmpty())
            return null;
        else
            return bridges.get(0);
    }

    private Direction getDirection(int mouseX, int mouseY, int islandX, int islandY) {
        int dx = mouseX - islandX;
        int dy = mouseY - islandY;
        if (Math.abs(dx) > 0 && Math.abs(dx) < gridTolerance
                && Math.abs(dy) > 0 && Math.abs(dy) < gridTolerance) {
            return Direction.nearest(dx, dy);
        } else
            return null;
    }

    /**
     * (Re)paints the board, that is: the grid, the bridges and the islands.
     */
    @Override
    public void paint(Graphics g) {
        if (!game.hasBoard())
            return;

        paintGrid(g);
        paintBridges(g);
        paintIslands(g);
    }


    /**
     * (Re)paint the grid.
     *
     * @param g - the Graphics instance to draw on.
     */
    private void paintGrid(Graphics g) {
        // draw grid
        g.setColor(gridColor);
        // horizontal lines
        for (int y = 0; y < game.getHeight(); y++) {
            Point p1 = this.getPixelPosition(0, y);
            Point p2 = this.getPixelPosition(game.getWidth() - 1, y);
            g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
        }

        // vertical lines
        for (int x = 0; x < game.getWidth(); x++) {
            Point p1 = this.getPixelPosition(x, 0);
            Point p2 = this.getPixelPosition(x, game.getHeight() - 1);
            g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
        }
    }

    /**
     * (Re)paint the islands.
     *
     * @param g - the Graphics instance to draw on.
     */
    private void paintIslands(Graphics g) {
        for (Island island : game.getIslands()) {
            int required = island.getRequiredBridges();
            int count = Bridge.count(game.getBridges(island));

            String label;
            Color labelColor;
            if (displayMissing) {
                label = Integer.toString(required - count);
                labelColor = displayMissingColor;
            } else {
                label = Integer.toString(required);
                labelColor = textColor;
            }

            if (count < required)
                paintIsland(g, island, islandColor, label, labelColor);
            else if (count == required)
                paintIsland(g, island, completeIslandColor, label, labelColor);
            else
                paintIsland(g, island, invalidIslandColor, label, labelColor);
        }
    }

    /**
     * Draw a single island.
     *
     * @param g          - the Graphics instance to draw on.
     * @param island     - the island to draw.
     * @param color      - the color to draw the island in.
     * @param label      - the label to draw over the island.
     * @param labelColor - the color of the label.
     */
    private void paintIsland(Graphics g, Island island, Color color, String label, Color labelColor) {
        Point p = getPixelPosition(island.getX(), island.getY());
        int x = (int) p.getX() - gridDiameter / 2;
        int y = (int) p.getY() - gridDiameter / 2;
        g.setColor(color);
        g.fillArc(x, y, gridDiameter, gridDiameter, 0, 360);
        g.setColor(labelColor);
        drawCenteredString(g, label, (int) p.getX(), (int) p.getY(), labelColor);
    }

    /**
     * Draw a single bridge.
     *
     * @param g      - the Graphics instance to draw on.
     * @param bridge - the bridge to draw.
     * @param color  - the color to draw the bridge in.
     */
    private void paintBridge(Graphics g, Bridge bridge, Color color) {
        Island island1 = bridge.getFirstIsland();
        Island island2 = bridge.getSecondIsland();
        Point p1 = getPixelPosition(island1.getX(), island1.getY());
        Point p2 = getPixelPosition(island2.getX(), island2.getY());

        int dx;
        int dy;

        if (bridge.isDouble()) {
            // vertical double bridge
            if (island1.getX() == island2.getX()) {
                dx = gridDiameter / 6;
                dy = 0;
            }
            // horizontal double bridge
            else {
                dx = 0;
                dy = gridDiameter / 6;
            }
            g.setColor(color);
            g.drawLine((int) p1.getX() - dx, (int) p1.getY() - dy, (int) p2.getX() - dx, (int) p2.getY() - dy);
            g.drawLine((int) p1.getX() + dx, (int) p1.getY() + dy, (int) p2.getX() + dx, (int) p2.getY() + dy);
        } else {
            g.setColor(color);
            g.drawLine((int) p1.getX(), (int) p1.getY(),
                    (int) p2.getX(), (int) p2.getY());
        }
    }

    /**
     * (Re)paint all bridges.
     *
     * @param g - the Graphics instance to draw the bridges on.
     */
    private void paintBridges(Graphics g) {
        for (Bridge bridge : game.getBridges())
            paintBridge(g, bridge, bridgeColor);

        // Draw the last bridge over the already drawn bridges.
        if (lastBridge != null)
            paintBridge(g, lastBridge, lastBridgeColor);

        // Draw the planned Bridge over every other bridges.
        if (selectedBridge != null)
            if (game.isValid(selectedBridge))
                paintBridge(g, selectedBridge, plannedBridgeColor);
            else
                paintBridge(g, selectedBridge, invalidBridgeColor);
    }

    /**
     * Helper method, that draws a String centered around the given position.
     *
     * @param g          - the Graphics instance to draw on.
     * @param text       - the String to draw.
     * @param pixelX     - the x coordinate.
     * @param pixelY     - the y coordinate.
     * @param labelColor - the color of the text to draw.
     */
    private void drawCenteredString(Graphics g, String text, int pixelX, int pixelY, Color labelColor) {
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text);
        int height = metrics.getAscent();
        g.setColor(labelColor);
        g.drawString(text, pixelX - width / 2, pixelY + height / 2);
    }

    /**
     * Return the centered pixel position of the given board position.
     *
     * @param boardX - the x coordinate on the board.
     * @param boardY - the y coordinate on the board.
     * @return The point which represents the given board position on the screen.
     */
    private Point getPixelPosition(int boardX, int boardY) {
        int x = (int) (MARGIN + gridOffsetX + boardX * gridStep);
        int y = (int) (MARGIN + gridOffsetY + boardY * gridStep);
        return new Point(x, y);
    }

    /**
     * Return the board position of a given screen position.
     *
     * @param pixelX - the x coordinate of the screen position.
     * @param pixelY - the y coordinate of the screen position.
     * @return The point which represents the given pixel position on the board.
     */
    private Point getBoardPosition(int pixelX, int pixelY) {
        int x = Math.round((pixelX - MARGIN - gridOffsetX) / gridStep);
        int y = Math.round((pixelY - MARGIN - gridOffsetY) / gridStep);
        return new Point(x, y);

    }

    /**
     * (Re)calculate the board grid.
     * <br/><br/>
     * Called, when this Canvas is resized.
     */
    private void calculateGrid() {
        // Get the width and height of the board and
        // recalculate the grid.
        boardWidth = game.getWidth();
        boardHeight = game.getHeight();

        int availableWidth = getWidth() - MARGIN * 2;
        int availableHeight = getHeight() - MARGIN * 2;
        int spacesX = boardWidth;
        int spacesY = boardHeight;

        float stepX = (float) availableWidth / spacesX;
        float stepY = (float) availableHeight / spacesY;

        // Set the distance between grid lines to the minimum of available space,
        // so the grid fits in x as well as in y direction on the screen.
        if (stepX > stepY) {
            gridStep = stepY;
            gridOffsetX = (int) (availableWidth - gridStep * spacesX) / 2;
            gridOffsetY = (int) gridStep / 2;
        } else {
            gridStep = stepX;
            gridOffsetX = (int) gridStep / 2;
            gridOffsetY = (int) (availableHeight - gridStep * spacesY) / 2;
        }

        gridDiameter = (int) (gridStep * 2) / 3;
        gridTolerance = (int) gridStep - 1;

        // System.out.println("MARGIN: " + MARGIN);
        // System.out.println("offsetX: " + gridOffsetX);
        // System.out.println("offsetY: " + gridOffsetY);
        // System.out.println("Board width (pixel): " + (int) gridStep * spacesX);
        // System.out.println("Board height (pixel): " + (int) gridStep * spacesY);
        // System.out.println("total width: " + getWidth());
        // System.out.println("total height: " + getHeight());
        // System.out.println("step: " + gridStep);
    }

    /**
     * Update the BoardCanvas after the game model has been changed.
     *
     * @param game - the game model, that has been changed.
     * @param obj  - the changed bridge (or null).
     */
    @Override
    public void update(Observable game, Object obj) {
        if (obj instanceof Bridge)
            lastBridge = (Bridge) obj;
        else
            lastBridge = null;

        // Deselect bridge.
        selectedBridge = null;

        // We have to call calculateGrid, since the whole
        // board could have been replaced with a new one.
        calculateGrid();
        repaint();
    }
}
