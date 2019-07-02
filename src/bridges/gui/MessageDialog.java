package bridges.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;

/**
 * Dialog class, that displays a simple message and an
 * "OK"-Button to close the window.
 *
 * @author Maik Messerschmidt
 */
public class MessageDialog extends CustomDialog {
    /**
     * Create a new MessageDialog.
     *
     * @param owner   - The owning Frame.
     * @param title   - The title this MessageDialog will have.
     * @param message - The message that will be displayed.
     */
    public MessageDialog(Frame owner, String title, String message) {
        // Make this dialog modal.
        super(owner, true, title);

        setLayout(new BorderLayout());
        /*
         * Since AWT doesn't provide labels that can be wrapped
         * we use a panel full of labels by splitting the message
         * at whitespace characters.
         */
        Panel textPanel = new Panel();
        textPanel.setLayout(new FlowLayout());
        add(textPanel, BorderLayout.CENTER);
        for (String word : message.split("\\s+"))
            textPanel.add(new Label(word));

        // Add "OK" button and make it close this dialog.
        Button okButton = new Button("OK");
        okButton.addActionListener(e -> setVisible(false));
        add(okButton, BorderLayout.SOUTH);
    }
}
