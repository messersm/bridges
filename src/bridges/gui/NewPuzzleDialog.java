package bridges.gui;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;


import bridges.model.GameModel;
import bridges.util.BoardGenerator;

/**
 * Class, that represents a dialog, which enables the user
 * to create a new puzzle with different parameters.
 * <br><br>
 * This class acts as a controller for the game model.
 *
 * @author Maik Messerschmidt
 */
public class NewPuzzleDialog extends CustomDialog {
    private GameModel game;
    private String error;

    private TextField widthField;
    private TextField heightField;
    private TextField islandCountField;

    /**
     * Create a new dialog.
     *
     * @param owner - the owner frame.
     * @param game  - the game model.
     */
    public NewPuzzleDialog(Frame owner, GameModel game) {
        // make this dialog modal
        super(owner, true, "New Puzzle", 0.8, 0.8);
        this.game = game;
        error = null;

        CheckboxGroup settings = new CheckboxGroup();
        Checkbox autoBox = new Checkbox("Randomize size and island count", settings, true);
        Checkbox manuelBox = new Checkbox("Customize size and island count", settings, false);
        Checkbox setIslandsBox = new Checkbox("Set island count");

        Label widthLabel = new Label("Width:");
        Label heightLabel = new Label("Height:");
        Label islandLabel = new Label("Island count:");

        widthField = new TextField();
        heightField = new TextField();
        islandCountField = new TextField();

        Button cancelButton = new Button("Cancel");
        Button confirmButton = new Button("OK");

        /* Set layout and add components using this layout:
         * +-------------------------------------+
         * | [autoBox]                           |
         * | [manuelBox]                         |
         * | +---------------------------------+ |
         * | | [widthLabel]  [widthField]      | |
         * | | [heightLabel] [heightField]     | |
         * | | [setIslandsBox]                 | |
         * | | [islandLabel] [islandCountField]| |                                       |
         * | +---------------------------------+ |
         * |                                     |
         * | +---------------------------------+ |
         * | |[cancelButton] [confirmButton]   | |
         * | +---------------------------------+ |
         * +-------------------------------------+
         */
        setLayout(new GridLayout(20, 1));
        add(autoBox);
        add(manuelBox);
        add(widthLabel);
        add(widthField);
        add(heightLabel);
        add(heightField);
        add(setIslandsBox);
        add(islandLabel);
        add(islandCountField);
        add(cancelButton);
        add(confirmButton);

        // Set default editable state.
        widthField.setEditable(false);
        heightField.setEditable(false);
        islandCountField.setEditable(false);
        setIslandsBox.setEnabled(false);

        cancelButton.addActionListener(e -> setVisible(false));
        confirmButton.addActionListener(e -> generateAndClose());

        // The event isn't fired, if the Checkbox is unselected via the CheckboxGroup, so
        // we need to add an ItemListener to autoBox _and_ manuelBox.
        autoBox.addItemListener(e -> widthField.setEditable(e.getStateChange() == 0));
        autoBox.addItemListener(e -> heightField.setEditable(e.getStateChange() == 0));
        autoBox.addItemListener(e -> setIslandsBox.setEnabled(e.getStateChange() == 0));
        manuelBox.addItemListener(e -> widthField.setEditable(e.getStateChange() == 1));
        manuelBox.addItemListener(e -> heightField.setEditable(e.getStateChange() == 1));
        manuelBox.addItemListener(e -> setIslandsBox.setEnabled(e.getStateChange() == 1));

        // Set the islandCountField to editable, if autoBox is not set and setIslandBox is set.
        autoBox.addItemListener(e -> islandCountField.setEditable(!autoBox.getState() && setIslandsBox.getState()));
        manuelBox.addItemListener(e -> islandCountField.setEditable(!autoBox.getState() && setIslandsBox.getState()));
        setIslandsBox.addItemListener(e -> islandCountField.setEditable(!autoBox.getState() && setIslandsBox.getState()));
    }

    /**
     * Correct errors in the input fields, if invalid input was given.
     * <p>
     * Note: This method is not in use right now.
     */
    private void fixInputFields() {
        TextField[] fields = {widthField, heightField, islandCountField};

        // Filter out, all characters, which are no digits for each field.
        for (TextField field : fields) {
            if (field.isEditable()) {
                String text = field.getText();
                field.setText(text.replaceAll("[^0-9]", ""));
            }
        }

        // If the input is still invalid, replace it by
        // a valid random value.
        int width;
        int height;
        int islandCount;

        if (!widthField.isEditable())
            return;

        try {
            width = Integer.parseInt(widthField.getText());
        } catch (NumberFormatException e) {
            width = BoardGenerator.randomWidth();
            widthField.setText(Integer.toString(width));
        }

        try {
            height = Integer.parseInt(heightField.getText());
        } catch (NumberFormatException e) {
            height = BoardGenerator.randomHeight();
            heightField.setText(Integer.toString(height));
        }

        if (!islandCountField.isEditable())
            return;

        try {
            islandCount = Integer.parseInt(islandCountField.getText());
        } catch (NumberFormatException e) {
            islandCount = BoardGenerator.randomIslandCount(width, height);
            islandCountField.setText(Integer.toString(islandCount));
        }
    }

    /**
     * Generate a new puzzle from the given parameters and close the dialog.
     */
    private void generateAndClose() {
        /*
         * Get input parameters for the puzzle generator
         * from the TextFields (we ignore spaces).
         *
         * If we encounter invalid input,
         * set an error message and return.
         */
        try {
            if (widthField.isEditable()) {
                int width = Integer.parseInt(widthField.getText().replaceAll(" ", ""));
                int height = Integer.parseInt(heightField.getText().replaceAll(" ", ""));

                if (islandCountField.isEditable()) {
                    int islandCount = Integer.parseInt(islandCountField.getText().replaceAll(" ", ""));
                    game.generate(width, height, islandCount);
                } else
                    game.generate(width, height, BoardGenerator.randomIslandCount(width, height));
            } else {
                int width = BoardGenerator.randomWidth();
                int height = BoardGenerator.randomHeight();
                int islandCount = BoardGenerator.randomIslandCount(width, height);
                game.generate(width, height, islandCount);
            }
            setVisible(false);
        }
        // It seems we can't create a new dialog from
        // a dialog, so we set an error message
        // and let the BridgesApp evaluate it.
        catch (NumberFormatException e) {
            // fixInputFields(); // No longer used.
            error = "Incorrect input: " + e.getMessage();
            setVisible(false);
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
            setVisible(false);
        } catch (RuntimeException e) {
            error = e.getMessage();
            setVisible(false);
        }
    }

    /**
     * Return the error string.
     *
     * @return A String representing errors, that occurred (or null).
     */
    public String getError() {
        return error;
    }
}
