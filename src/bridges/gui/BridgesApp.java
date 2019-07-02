package bridges.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Toolkit;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;
import java.util.logging.Level;

import bridges.game.BoardState;
import bridges.game.Bridge;
import bridges.game.Island;
import bridges.gui.BoardCanvas;
import bridges.model.GameModel;

/**
 * Represents a Bridges Game Application.
 *
 * @author Maik Messerschmidt
 */
public class BridgesApp extends Frame implements Observer {
    private final Logger logger = Logger.getLogger("bridges.gui.BridgesApp");

    private String puzzleFilename = null;
    private BoardCanvas boardCanvas;
    private Button solveButton;
    private Label solveLabel;
    private GameModel game;
    private boolean isSolving = false;
    private int solveInterval = 5000;

    /**
     * Create a new BridgesApp instance.
     */
    public BridgesApp() {
        // Add event handler to close the application.
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        logger.setLevel(Level.FINEST);

        game = new GameModel();
        game.addObserver(this);

        Thread solver = new Thread(() -> runSolveThread());
        solver.start();

        // Set title, screen position (centered) and dimension (screen width * 2/3, height * 2/3).
        this.setTitle("Bridges");
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        this.setSize((int) dim.getWidth() * 2 / 3, (int) dim.getHeight() * 2 / 3);
        this.setLocation((int) dim.getWidth() / 6, (int) dim.getHeight() / 6);

        /*
         * Create the file menu and adds entries to it.
         *
         * Since menu.add() returns the newly created MenuItem,
         * we directly call addActionListener and add the callback
         * to a method of the BridgesApp object using Lambda expressions.
         */
        Menu menu = new Menu("Datei");
        menu.add(new MenuItem("Neues Rätsel")).addActionListener(
                e -> BridgesApp.this.newPuzzle());
        menu.add(new MenuItem("Rätsel neu starten")).addActionListener(
                e -> BridgesApp.this.restartPuzzle());
        menu.addSeparator();
        menu.add(new MenuItem("Rätsel laden")).addActionListener(
                e -> BridgesApp.this.loadPuzzle());
        menu.add(new MenuItem("Neues Rätsel aus zwei bestehenden Rätseln")).addActionListener(
                e -> BridgesApp.this.loadMultiple());
        menu.add(new MenuItem("Rätsel speichern")).addActionListener(
                e -> BridgesApp.this.savePuzzle());
        menu.add(new MenuItem("Rätsel speichern unter")).addActionListener(
                e -> BridgesApp.this.savePuzzleAs());

        // Create close entry and run System.exit(0) if chosen.
        menu.addSeparator();
        menu.add(new MenuItem("Beenden")).addActionListener(
                e -> System.exit(0));

        MenuBar menubar = new MenuBar();
        menubar.add(menu);
        this.setMenuBar(menubar);

        /* Set layout and add components using this layout:
         * +-------------------------------------+
         * | [MenuBar] (MENUBAR)                 |
         * +-------------------------------------+
         * | [gamePanel] (CENTER)                |
         * | +---------------------------------+ |
         * | | [boardCanvas] (CENTER)          | |
         * | | [controlPanel] (SOUTH)          | |
         * | | +-----------------------------+ | |
         * | | | [showMissingBox] (0, 0)     | | |
         * | | | [buttonPanel] (0, 1)        | | |
         * | | | +-------------------------+ | | |
         * | | | |[solveButton][nextButton]| | | |
         * | | | +-------------------------+ | | |
         * | | +-----------------------------+ | |
         * | +---------------------------------+ |
         * |                                     |
         * +-------------------------------------+
         * | [statusLabel] (SOUTH)               |
         * +-------------------------------------+
         */

        // Create components
        Panel gamePanel = new Panel();
        Panel controlPanel = new Panel();
        Panel buttonPanel = new Panel();
        Panel statusPanel = new Panel();

        boardCanvas = new BoardCanvas(game);

        Checkbox showMissingBox = new Checkbox("Anzahl fehlender Brücken anzeigen");

        solveButton = new Button("Automatisch lösen");
        Button nextButton = new Button("Nächste Brücke");

        Label statusLabel = new StatusLabel(game);
        solveLabel = new Label();
        solveLabel.setText("");
        solveLabel.setAlignment(Label.RIGHT);

        // Set layout
        setLayout(new BorderLayout());
        gamePanel.setLayout(new BorderLayout());
        controlPanel.setLayout(new GridLayout(2, 1));
        buttonPanel.setLayout(new GridLayout(1, 2));
        statusPanel.setLayout(new GridLayout(1, 2));

        // Add components
        add(gamePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        gamePanel.add(boardCanvas, BorderLayout.CENTER);
        gamePanel.add(controlPanel, BorderLayout.SOUTH);
        controlPanel.add(showMissingBox);
        controlPanel.add(buttonPanel);
        buttonPanel.add(solveButton, 0, 0);
        buttonPanel.add(nextButton, 0, 1);
        statusPanel.add(statusLabel, 0, 0);
        statusPanel.add(solveLabel, 0, 1);

        // Setup behavior
        showMissingBox.addItemListener(
                e -> boardCanvas.displayMissing(showMissingBox.getState()));
        solveButton.addActionListener(e -> toggleSolving());
        nextButton.addActionListener(e -> nextStep());

    }

    /**
     * The method that is run by the solve thread.
     */
    private void runSolveThread() {
        while (true) {
            if (isSolving) {
                solveLabel.setText("Löse...");
                boolean success = game.nextStep();
                solveLabel.setText("");
                if (success) {
                    try {
                        logger.fine("Solve thread sleeping after successful step.");
                        Thread.sleep(solveInterval);
                    }
                    // End execution, if called for.
                    catch (InterruptedException e) {
                        break;
                    }
                } else {
                    logger.info("Solve thread: nextStep() failed.");
                    BoardState state = game.getState();
                    Dialog dialog = null;
                    switch (state) {
                        case NOBOARD:
                            toggleSolving(false);
                            break;
                        case UNSOLVABLE:
                            toggleSolving(false);
                            dialog = new MessageDialog(
                                    this, "Hinweis", "Das Spiel ist nicht mehr lösbar.");
                            break;
                        case INCORRECT:
                            toggleSolving(false);
                            dialog = new MessageDialog(
                                    this, "Hinweis", "Keine weiteren Lösungschritte möglich (Fehler vorhanden).");
                            break;
                        case SOLVED:
                            toggleSolving(false);
                            dialog = new MessageDialog(
                                    this, "Hinweis", "Spiel gelöst.");
                        default:
                            break;
                    }
                    if (dialog != null)
                        dialog.setVisible(true);
                }
            } else {
                try {
                    logger.fine("Solve thread waiting.");
                    Thread.sleep(100);
                }
                // End execution, if called for.
                catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * Activate or deactivate automatic solving.
     */
    private void toggleSolving() {
        if (isSolving)
            toggleSolving(false);
        else
            toggleSolving(true);
    }

    /**
     * Activate or deactivate automatic solving.
     *
     * @param solving - whether or not to solve automatically.
     */
    private void toggleSolving(boolean solving) {
        isSolving = solving;
        if (isSolving)
            solveButton.setLabel("Lösen anhalten.");
        else
            solveButton.setLabel("Automatisch lösen");
    }

    /**
     * Apply the next solve step to the board.
     */
    private void nextStep() {
        if (game.nextStep() == false) {
            Dialog dialog = new MessageDialog(
                    this, "Hinweis", "Es sind keine weiteren Lösungsschritte möglich.");
            dialog.setVisible(true);
        }
    }

    /**
     * Open a new puzzle dialog.
     */
    private void newPuzzle() {
        toggleSolving(false);
        NewPuzzleDialog dialog = new NewPuzzleDialog(this, game);
        dialog.setVisible(true);
        String error = dialog.getError();
        if (error != null) {
            // Display error and try again.
            Dialog errorDialog = new MessageDialog(this, "Fehler", error);
            errorDialog.setVisible(true);
            newPuzzle();
        }
    }

    /**
     * Restart the current puzzle.
     */
    private void restartPuzzle() {
        toggleSolving(false);
        game.restart();
    }

    /**
     * Open a load puzzle dialog.
     */
    private void loadPuzzle() {
        /*
         * Create a new file dialog and load the puzzle referenced by
         * filename, unless the choice was cancelled (filename == null).
         */
        String fullname = chooseLoadFile("Neues Rätsel laden");

        if (fullname != null) {
            try {
                game.load(fullname);

                // We only set the puzzleFilename, if the board already
                // has bridges (that is: a game is continued from a save
                // file, instead of being started.
                if (game.getBridges().size() > 0)
                    puzzleFilename = fullname;
                else
                    puzzleFilename = null;
            } catch (IOException | IllegalArgumentException e) {
                Dialog errDialog = new MessageDialog(this, "Fehler",
                        "Fehler beim lesen der Datei " + fullname + ":\n\n" + e.getMessage());
                errDialog.setVisible(true);
            }
        }
    }

    /**
     * Open a load dialog and return the given filename (or null).
     * <p>
     * This is a helper function for loadPuzzle() and loadMultiple().
     * <p>
     * Note: This will pause the automatic solving of the current puzzle,
     * in case it is running.
     *
     * @param message The message to display for the dialog.
     * @return The filename, that was chosen by the file dialog (or null).
     */
    private String chooseLoadFile(String message) {
        /*
         * Create a new file dialog and load the puzzle referenced by
         * filename, unless the choice was cancelled (filename == null).
         */
        toggleSolving(false);
        FileDialog dialog = new FileDialog(this, message, FileDialog.LOAD);
        dialog.setFile("*.bgs");
        dialog.setVisible(true);
        String filename = dialog.getFile();
        String dirname = dialog.getDirectory();

        if (filename == null)
            return null;
        else {
            String fullname = dirname + filename;
            return fullname;
        }
    }

    /**
     * Save the current puzzle, opening a dialog, if no filename has been given yet.
     */
    private void savePuzzle() {
        toggleSolving(false);
        if (puzzleFilename == null)
            this.savePuzzleAs();
        else
            writePuzzleFile();
    }

    /**
     * Write the board to puzzleFilename and inform the user, if it failed.
     */
    private void writePuzzleFile() {

        try {
            game.save(puzzleFilename);
        } catch (IOException | IllegalArgumentException e) {
            MessageDialog dialog = new MessageDialog(this, "Fehler",
                    "Datei '" + puzzleFilename + "' konnte nicht gespeichert werden: " +
                            e.getMessage());
            dialog.setVisible(true);
        }
    }

    /**
     * Open a new save as dialog.
     */
    private void savePuzzleAs() {
        /*
         * Create a new file dialog and load the puzzle referenced by
         * filename, unless the choice was cancelled (filename == null).
         */
        toggleSolving(false);
        FileDialog dialog = new FileDialog(this, "Rätsel speichern", FileDialog.SAVE);
        dialog.setFile("*.bgs");
        dialog.setVisible(true);
        String filename = dialog.getFile();
        String dirname = dialog.getDirectory();

        if (filename != null) {
            // Set the filename and call writePuzzleFile()
            puzzleFilename = dirname + filename;
            writePuzzleFile();
        }
    }

    /**
     * Update the BridgesApp after the game model has been changed.
     * <br><br>
     * This method is not used right now (meaning the bridges application
     * is only a controller and no view, speaking in MVC terms).
     *
     * @param o   - the game model, that has been changed.
     * @param obj - the changed bridge (or null).
     */
    @Override
    public void update(Observable o, Object obj) {

    }

    /**
     * Open a dialog to load two files and merge them.
     */
    public void loadMultiple() {
        /*
         * Create a new file dialog and load the puzzle referenced by
         * filename, unless the choice was cancelled (filename == null).
         */
        String left = chooseLoadFile("Linkes Rätsel laden");
        if (left == null)
            return;

        String right = chooseLoadFile("Rechtes Rätsel laden");
        if (right == null)
            return;

        try {
            game.merge(left, right);

            // We set puzzleFilename to null, since the resulting puzzle
            // is new.
            puzzleFilename = null;

        } catch (IOException | IllegalArgumentException e) {
            Dialog errDialog = new MessageDialog(this, "Fehler",
                    "Fehler beim Zusammenführen der Dateien:\n\n" + e.getMessage());
            errDialog.setVisible(true);
        }
    }
}
