package bridges.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The CustomDialog provides some basic functionality used by: <br>
 * - MessageDialog and <br>
 * - NewPuzzleDialog <br>
 * <br>
 * like: <br>
 * - setting the title <br>
 * - closing the dialog, when the user presses 'X' and <br>
 * - scaling and positioning the window.
 *
 * @author Maik Messerschmidt
 */
abstract public class CustomDialog extends Dialog {
    final private static double DEFAULT_WIDTH_RATIO = 0.5;
    final private static double DEFAULT_HEIGHT_RATIO = 0.5;
    final private static String DEFAULT_TITLE = "";
    final private static boolean DEFAULT_MODAL = true;

    /**
     * Create a new Dialog with the given owner.
     *
     * @param owner The owner frame.
     */
    public CustomDialog(Frame owner) {
        this(owner, DEFAULT_MODAL);
    }

    /**
     * Create a new Dialog with the given owner and modality.
     *
     * @param owner - the owner frame.
     * @param modal - whether or not this dialog should block access to the owner.
     */
    public CustomDialog(Frame owner, boolean modal) {
        this(owner, modal, DEFAULT_TITLE);
    }

    /**
     * Create a new Dialog with the given owner and modality and title.
     *
     * @param owner - the owner frame.
     * @param modal - whether or not this dialog should block access to the owner.
     * @param title - the title of the new dialog.
     */
    public CustomDialog(Frame owner, boolean modal, String title) {
        this(owner, modal, title, DEFAULT_WIDTH_RATIO, DEFAULT_HEIGHT_RATIO);
    }

    /**
     * Create a new Dialog with the given owner and modality and title and size information.
     *
     * @param owner       - the owner frame.
     * @param modal       - whether or not this dialog should block access to the owner.
     * @param title       - the title of the new dialog.
     * @param widthRatio  - indicates how many percent of the width of the owner frame is used for the width,
     *                    so if the owner has a width of 600 pixel and widthRatio is set to 0.5 the dialog
     *                    will have a width of 300 pixel.
     * @param heightRatio - same as widthRatio but for the height.
     */
    public CustomDialog(Frame owner, boolean modal, String title, double widthRatio, double heightRatio) {
        super(owner, modal);

        // Add event handler to close this dialog.
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        // Set title, position (centered to the owner frame) and size.
        this.setTitle(title);
        Point framePos = owner.getLocation();
        Dimension frameSize = owner.getSize();

        this.setSize(
                (int) (frameSize.getWidth() * widthRatio),
                (int) (frameSize.getHeight() * heightRatio));

        // Locate this dialog centered to the owner frame.
        int x = (int) (framePos.getX() + frameSize.getWidth() / 2 - getWidth() / 2);
        int y = (int) (framePos.getY() + frameSize.getHeight() / 2 - getHeight() / 2);
        setLocation(x, y);
    }
}
