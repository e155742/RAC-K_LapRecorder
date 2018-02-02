package net.rack;

import java.awt.EventQueue;

import javax.swing.JFrame;

/**
 * @author e155742
 *
 */
@SuppressWarnings("serial")
public class Main extends JFrame {
    public static final String SOFTWARE_TITLE = "RAC-K LapRecorder";
    public static final int    HEIGHT         = 480;
    public static final int    WIDTH          = 800;

    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {
            // @Override
            public void run() {
                new MyFrame(SOFTWARE_TITLE, WIDTH, HEIGHT).setVisible(true);
            }
        });
    }
}
