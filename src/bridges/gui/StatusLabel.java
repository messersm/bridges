package bridges.gui;

import java.awt.Label;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import bridges.game.BoardState;
import bridges.model.GameModel;

/**
 * Represents the status label of the application.
 * <br><br>
 * This class acts as an observer for the game model.
 *
 * @author Maik Messerschmidt
 */
public class StatusLabel extends Label implements Observer {
    Map<BoardState, String> messages = new HashMap<BoardState, String>();

    /**
     * Create a new status label.
     *
     * @param game - the game model.
     */
    public StatusLabel(GameModel game) {
        // Make sure, the messages are added, before adding
        // us as observer - otherwise a thread could call
        // update, before we have all required values.
        messages.put(BoardState.NOBOARD, "No puzzle loaded");
        messages.put(BoardState.INCORRECT, "Incorrect bridges detected.");
        messages.put(BoardState.SOLVED, "Puzzle solved.");
        messages.put(BoardState.UNSOLVABLE, "Puzzle is no longer solvable.");
        messages.put(BoardState.UNSOLVED, "Puzzle not solved yet.");
        game.addObserver(this);

        // Call update once, to set the correct label.
        update(game, null);
    }

    /**
     * Update the status label after the game model has been changed.
     *
     * @param o   - the game model, that has been changed.
     * @param obj - the changed bridge (or null).
     */
    @Override
    public void update(Observable o, Object obj) {
        if (o instanceof GameModel) {
            GameModel game = (GameModel) o;
            this.setText(messages.get(game.getState()));
        }
    }
}
